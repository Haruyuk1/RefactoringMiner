package gr.uom.java.xmi.decomposition;

import java.util.ArrayList;
import java.util.List;

import com.intellij.psi.*;

import gr.uom.java.xmi.*;
import gr.uom.java.xmi.LocationInfo.CodeElementType;
import gr.uom.java.xmi.diff.CodeRange;

public class LambdaExpressionObject implements LocationInfoProvider {
	private LocationInfo locationInfo;
	private OperationBody body;
	private AbstractExpression expression;
	private List<VariableDeclaration> parameters = new ArrayList<VariableDeclaration>();
	private List<UMLParameter> umlParameters = new ArrayList<UMLParameter>();
	private boolean hasParentheses = false;
	
	public LambdaExpressionObject(PsiFile cu, String filePath, PsiLambdaExpression lambda) {
		this.locationInfo = new LocationInfo(cu, filePath, lambda, CodeElementType.LAMBDA_EXPRESSION);
		if(lambda.getBody() instanceof PsiCodeBlock) {
			this.body = new OperationBody(cu, filePath, (PsiCodeBlock)lambda.getBody());
		}
		else if(lambda.getBody() instanceof PsiExpression) {
			this.expression = new AbstractExpression(cu, filePath, (PsiExpression)lambda.getBody(), CodeElementType.LAMBDA_EXPRESSION_BODY);
		}
		this.hasParentheses = lambda.hasFormalParameterTypes();
		PsiParameterList params = lambda.getParameterList();
		for(PsiParameter param : params.getParameters()) {
			VariableDeclaration parameter = new VariableDeclaration(cu, filePath, param, CodeElementType.LAMBDA_EXPRESSION_PARAMETER);
			this.parameters.add(parameter);
			if(param.getTypeElement() != null) {
				String parameterName = param.getName();
				UMLType type = UMLTypePsiParser.extractTypeObject(cu, filePath, param.getTypeElement(), param.getType());
				UMLParameter umlParameter = new UMLParameter(parameterName, type, "in", param.isVarArgs());
				umlParameter.setVariableDeclaration(parameter);
				this.umlParameters.add(umlParameter);
			}
		}
	}

	public LambdaExpressionObject(PsiFile cu, String filePath, PsiMethodReferenceExpression reference) {
		this.locationInfo = new LocationInfo(cu, filePath, reference, CodeElementType.LAMBDA_EXPRESSION);
		this.expression = new AbstractExpression(cu, filePath, reference, CodeElementType.LAMBDA_EXPRESSION_BODY);
	}
	
	public OperationBody getBody() {
		return body;
	}

	public AbstractExpression getExpression() {
		return expression;
	}

	public List<VariableDeclaration> getParameters() {
		return parameters;
	}

	public List<UMLParameter> getUmlParameters() {
		return umlParameters;
	}

	public List<String> getParameterNameList() {
		List<String> parameterNameList = new ArrayList<String>();
		for(VariableDeclaration parameter : parameters) {
			parameterNameList.add(parameter.getVariableName());
		}
		return parameterNameList;
	}

	public List<UMLType> getParameterTypeList() {
		List<UMLType> parameterTypeList = new ArrayList<UMLType>();
		for(UMLParameter parameter : umlParameters) {
			parameterTypeList.add(parameter.getType());
		}
		return parameterTypeList;
	}

	public int getNumberOfNonVarargsParameters() {
		int counter = 0;
		for(UMLParameter parameter : umlParameters) {
			if(!parameter.isVarargs()) {
				counter++;
			}
		}
		return counter;
	}

	public boolean hasVarargsParameter() {
		for(UMLParameter parameter : umlParameters) {
			if(parameter.isVarargs()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public LocationInfo getLocationInfo() {
		return locationInfo;
	}

	public CodeRange codeRange() {
		return locationInfo.codeRange();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locationInfo == null) ? 0 : locationInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LambdaExpressionObject other = (LambdaExpressionObject) obj;
		if (locationInfo == null) {
			if (other.locationInfo != null)
				return false;
		} else if (!locationInfo.equals(other.locationInfo))
			return false;
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(hasParentheses) {
			sb.append("(");
		}
		for(int i=0; i<parameters.size(); i++) {
			sb.append(parameters.get(i).getVariableName());
			if(i < parameters.size()-1)
				sb.append(", ");
		}
		if(hasParentheses) {
			sb.append(")");
		}
		if(parameters.size() > 0 || hasParentheses) {
			sb.append(" -> ");
		}
		if(expression != null) {
			sb.append(expression.getString());
		}
		else if(body != null) {
			List<String> statements = body.stringRepresentation();
			for(String statement : statements) {
				sb.append(statement);
			}
		}
		return sb.toString();
	}
}
