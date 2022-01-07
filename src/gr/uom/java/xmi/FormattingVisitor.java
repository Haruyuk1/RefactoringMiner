package gr.uom.java.xmi;

import com.intellij.psi.*;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Visitor for building string representation of PsiElement
 * 1. Without commentaries
 * 2. With formatting similar to Eclipse format
 */
public class FormattingVisitor extends PsiRecursiveElementWalkingVisitor {
    private static final TokenSet noSpaces = TokenSet.create(
        JavaTokenType.EQ, JavaTokenType.LT, JavaTokenType.GT,
        JavaTokenType.DOT, JavaTokenType.COMMA,
        JavaTokenType.RBRACKET, JavaTokenType.LBRACKET,
        JavaTokenType.LPARENTH, JavaTokenType.RPARENTH,
        JavaTokenType.SEMICOLON, JavaTokenType.DOUBLE_COLON
    );
    private static final TokenSet noSpaceAfter = TokenSet.create(
        JavaTokenType.AT, JavaTokenType.LBRACE, JavaTokenType.RBRACE, JavaTokenType.EXCL
    );
    private static final TokenSet noSpaceBefore = TokenSet.create(
            JavaTokenType.ELLIPSIS
    );
    private static final TokenSet endLineAfter = TokenSet.create(
            JavaTokenType.LBRACE, JavaTokenType.RBRACE, JavaTokenType.SEMICOLON
    );
    private static final TokenSet annotationNoSpaceBefore = TokenSet.create(
        JavaTokenType.ELLIPSIS, JavaTokenType.RBRACE
    );
    private static final TokenSet annotationEndLineAfter = TokenSet.create(
        JavaTokenType.SEMICOLON
    );
    private static final TokenSet conditionalExpressionTokens = TokenSet.create(
            JavaTokenType.QUEST, JavaTokenType.COLON
    );
    /*
    *  TIMES
    /  DIVIDE
    %  REMAINDER
    +  PLUS
    -  MINUS
    <<  LEFT_SHIFT
    >>  RIGHT_SHIFT_SIGNED
    >>>  RIGHT_SHIFT_UNSIGNED
    <  LESS
    >  GREATER
    <=  LESS_EQUALS
    >=  GREATER_EQUALS
    ==  EQUALS
    !=  NOT_EQUALS
    ^  XOR
    &  AND
    |  OR
    &&  CONDITIONAL_AND
    ||  CONDITIONAL_OR
     */
    private static final TokenSet infixOperators = TokenSet.create(
            JavaTokenType.ASTERISK, JavaTokenType.DIV, JavaTokenType.PERC, JavaTokenType.PLUS, JavaTokenType.MINUS,
            JavaTokenType.LTLT, JavaTokenType.GTGT, JavaTokenType.GTGTGT,
            JavaTokenType.LT, JavaTokenType.GT, JavaTokenType.LE, JavaTokenType.GE, JavaTokenType.EQEQ, JavaTokenType.NE,
            JavaTokenType.XOR, JavaTokenType.AND, JavaTokenType.OR, JavaTokenType.ANDAND, JavaTokenType.OROR
    );

    private final StringBuilder sb = new StringBuilder();
    private boolean previousNeedSpaceAfter = false;

    @Override
    public void visitElement(@NotNull PsiElement element) {
        if (element.getFirstChild() == null) {
            // Add text from leaves. Excluding comments and user-specific whitespaces
            if (!(element instanceof PsiWhiteSpace || element instanceof PsiComment)) {
                String text = element.getText();
                if (!text.isEmpty()) {
                    if ((needSpaceBefore(element) && previousNeedSpaceAfter) || mustHaveSpaceBefore(element)) {
                        sb.append(' ');
                    }
                    if(sb.toString().equals("return") && !text.equals(";")) {
                        sb.append(' ');
                    }
                    sb.append(element.getText());
                    if (needEndLineAfter(element)) {
                        sb.append('\n');
                    }
                    previousNeedSpaceAfter = needSpaceAfter(element);
                }
            }
        } else {
            super.visitElement(element);
        }
    }

    private static boolean needEndLineAfter(@NotNull PsiElement element) {
        return PsiUtil.isJavaToken(element,
                insideAnnotationOrArrayInitializer(element) ? annotationEndLineAfter : endLineAfter);
    }

    private static boolean needSpaceBefore(PsiElement element) {
        return !(PsiUtil.isJavaToken(element, noSpaces) || PsiUtil.isJavaToken(element,
                insideAnnotationOrArrayInitializer(element) ? annotationNoSpaceBefore : noSpaceBefore));
    }

    private static boolean needSpaceAfter(PsiElement element) {
        return !(PsiUtil.isJavaToken(element, noSpaces) || PsiUtil.isJavaToken(element, noSpaceAfter)) || isInfixOperator(element);
    }

    private static boolean mustHaveSpaceBefore(PsiElement element) {
        return isConditionalExpressionToken(element) || isLocalVariableDeclaration(element) || isMethodName(element) || isInfixOperator(element);
    }

    private static boolean isConditionalExpressionToken(PsiElement element) {
        return PsiUtil.isJavaToken(element, conditionalExpressionTokens) && element.getParent() instanceof PsiConditionalExpression;
    }

    private static boolean isInfixOperator(PsiElement element) {
        return PsiUtil.isJavaToken(element, infixOperators) &&
                (element.getParent() instanceof PsiBinaryExpression || element.getParent() instanceof PsiPolyadicExpression);
    }

    private static boolean isMethodName(PsiElement element) {
        return element instanceof PsiIdentifier && element.getParent() instanceof PsiMethod;
    }

    private static boolean isLocalVariableDeclaration(PsiElement element) {
        return element instanceof PsiIdentifier && element.getParent() instanceof PsiLocalVariable &&
                element.getParent().getParent() instanceof PsiDeclarationStatement;
    }

    private static boolean insideAnnotationOrArrayInitializer(PsiElement node) {
        PsiElement parent = node.getParent();
        while(parent != null) {
            if(parent instanceof PsiAnnotation || parent instanceof PsiArrayInitializerExpression) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public String getText() {
        return sb.toString();
    }
}
