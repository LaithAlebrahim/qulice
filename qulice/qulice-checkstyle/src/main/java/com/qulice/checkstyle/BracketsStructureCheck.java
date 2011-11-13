/**
 * Copyright (c) 2011, Qulice.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the Qulice.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.qulice.checkstyle;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Checks opening/closing brackets to be the last symbols on the line.
 *
 * <p>So this will do:
 *
 * <pre>
 * String.format(
 *   "File %s not found",
 *   file
 * );
 * String.format(
 *   "File %s not found", file
 * );
 * String.format("File %s not found", file);
 * </pre>
 *
 * <p>and this won't:
 *
 * <pre>
 * String.format("File %s not found",
 *   file);
 * String.format(
 *   "File %s not found",
 *   file);
 * String.format(
 *   "File %s not found", file);
 * </pre>
 *
 * @author Dmitry Bashkin (dmitry.bashkin@qulice.com)
 * @version $Id$
 * @todo #32:1h! Checks only method calls inside method bodies,
 *  constructors, static initializers, and instance initializers. We should
 *  extend its functionality and enable checking of all other language
 *  constructs.
 */
public final class BracketsStructureCheck extends Check {

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getDefaultTokens() {
        return new int[] {
            TokenTypes.METHOD_DEF,
            TokenTypes.STATIC_INIT,
            TokenTypes.INSTANCE_INIT,
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitToken(final DetailAST ast) {
        final DetailAST list = ast.findFirstToken(TokenTypes.SLIST);
        if (null != list) {
            DetailAST expression = list.findFirstToken(TokenTypes.EXPR);
            while (null != expression) {
                final DetailAST methodCall =
                    expression.findFirstToken(TokenTypes.METHOD_CALL);
                if (null != methodCall) {
                    this.checkMethod(methodCall);
                }
                expression = expression.getNextSibling();
            }
        }
    }

    /**
     * Checks method call statement to satisfy the rule.
     * @param methodCall Tree node, containing method call statement.
     */
    private void checkMethod(final DetailAST methodCall) {
        final DetailAST closing = methodCall.findFirstToken(TokenTypes.RPAREN);
        final int startLine = methodCall.getLineNo();
        final int endLine = closing.getLineNo();
        if (startLine != endLine) {
            final DetailAST elist = methodCall.findFirstToken(TokenTypes.ELIST);
            final int parametersLine = elist.getLineNo();
            if (parametersLine == startLine) {
                this.log(parametersLine, "Brackets structure is broken");
            }
            final DetailAST lastParameter = elist.getLastChild();
            final int lastParameterLine = lastParameter.getLineNo();
            if (lastParameterLine == endLine) {
                this.log(lastParameterLine, "Brackets structure broken");
            }
        }
    }
}