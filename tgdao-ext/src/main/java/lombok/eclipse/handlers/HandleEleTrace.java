package lombok.eclipse.handlers;

import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.eclipse.DeferUntilPostDiet;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import me.ele.lombok.EleTrace;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.mangosdk.spi.ProviderFor;

import java.util.Arrays;

import static lombok.core.handlers.HandlerUtil.*;
import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.eclipse.Eclipse.fromQualifiedName;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;
import static lombok.eclipse.handlers.EclipseHandlerUtil.createNameReference;
import static lombok.eclipse.handlers.EclipseHandlerUtil.setGeneratedBy;
import static me.ele.lombok.common.CommonUsage.ELETRACE_FLAG_USAGE;

/**
 * 
 * @EleTrace（type="type", name="name"）
 * public void func() throws Throwable {
 *   doSomething....
 * }
 * 
 * 将会变为
 * 
 * public void func() throws Throwable {
 *
 *       Transaction transaction = Trace.newTransaction(type, name);
 *       Exception exception = null;
 *       try{
 *			 doSomething....
 *       }catch (Exception e){
 *          exception = e;
 *          throw e;  //或不throw，代码中会判断是否有throws语句
 *       }finally {
 *          if (exception == null) {
 *              transaction.setStatus(SUCCESS);
 *          } else {
 *              transaction.setStatus(exception);
 *          }
 *          transaction.complete();
 *       }
 *   }
 *   
 * @author vincent
 *
 */
@ProviderFor(EclipseAnnotationHandler.class)
@DeferUntilPostDiet
@HandlerPriority(value = 1024)
public class HandleEleTrace extends EclipseAnnotationHandler<EleTrace>{

	@Override
	public void handle(final AnnotationValues<EleTrace> annotation, final Annotation ast,
			final EclipseNode annotationNode) {
		
	    handleFlagUsage(annotationNode, ELETRACE_FLAG_USAGE,"@EleTrace");
		String type = annotation.getInstance().type();
		if (type.isEmpty()) {
			//TODO
		}
		
		String name = annotation.getInstance().name();
		if (name.isEmpty()) {
			//TODO
		}
		
		EclipseNode owner = annotationNode.up();
		ASTNode blockNode =  owner.get();
		boolean hasThrows = false; //是否抛出throw语句
		
		switch (owner.getKind()) {
	     
		case METHOD:
	         
		    hasThrows = checkIfHasThrows(blockNode);
	    	    //获取块内容
		    final Statement[] statements = ((AbstractMethodDeclaration)blockNode).statements;
		    if (statements == null) {
		    	annotationNode.addError("LOMBOK BUG: Parent block does not contain any statements.");
		    	return;
		    }
		    	
		    //生成语句Transaction transaction = Trace.newTransaction....
		    LocalDeclaration transaction = createTransactionField(ast, type, name);
		    
		    int pS = ast.sourceStart, pE = ast.sourceEnd;
		    // 定义局部变量Exception exception = null;, 用于记录exception
		    LocalDeclaration exceptionDecl = new LocalDeclaration("exception".toCharArray(), pS, pE);
		    setGeneratedBy(exceptionDecl, ast);
		    NullLiteral nullLiteral = new NullLiteral(pS, pE);  //等同于null
		    setGeneratedBy(nullLiteral, ast);
		    exceptionDecl.initialization = nullLiteral;  //初始化null
		    exceptionDecl.type = createTypeReference("java.lang.Exception", ast); //类型
		    exceptionDecl.type.sourceStart = pS; exceptionDecl.type.sourceEnd = pE;

		    /************************ try语句块 ****************************/
		    TryStatement tryStatement = new TryStatement();
		    setGeneratedBy(tryStatement, ast);
		    tryStatement.tryBlock = new Block(0);
		    tryStatement.tryBlock.statements = statements;
		    setGeneratedBy(tryStatement.tryBlock, ast);

		    Statement[] newStatements = new Statement[3];
		    newStatements[0] = transaction;
		    newStatements[1] = exceptionDecl;
		    /**
		     * 因为定义了两个局部变量:transaction和exceptionDecl
		     * 这里其实不太确定，这里是按照lombok @Cleanup写的，其实一直没弄清sourceStart/sourceEnd表示
		     * 的意思，猜想是代码的影响范围...
		     */
		    int ss = transaction.declarationEnd + exceptionDecl.declarationEnd + 1;
		    int se = statements[statements.length - 1].sourceEnd + 1;
		    tryStatement.sourceStart = ss;
		    tryStatement.sourceEnd = se;
		    tryStatement.tryBlock.sourceStart = ss; 
		    tryStatement.tryBlock.sourceEnd = se;
		    newStatements[2] = tryStatement;

		    /************************ catch语句块 ****************************/
		    //catch语句块 catch (Exception e)
		    TypeReference typeReference = createTypeReference("java.lang.Exception", ast);;
		    Argument catchArg = new Argument("e".toCharArray(), se, typeReference, Modifier.FINAL);
		    setGeneratedBy(catchArg, ast);
		    catchArg.declarationSourceEnd = catchArg.declarationEnd = catchArg.sourceEnd = se;
		    catchArg.declarationSourceStart = catchArg.modifiersSourceStart = catchArg.sourceStart = se;
		    tryStatement.catchArguments = new Argument[] { catchArg };

		    //获得变量的引用
		    SingleNameReference e = getNameReference("e", ast);
		    SingleNameReference exception = getNameReference("exception", ast);
		    // exception = e; 赋值语句
		    Assignment assignment = new Assignment(exception, e, pE);
		    assignment.sourceStart = pS; assignment.sourceEnd = assignment.statementEnd = pE;
		    setGeneratedBy(assignment, ast);

		    Block block = new Block(0);
		    block.sourceStart = se;
		    block.sourceEnd = se;
		    setGeneratedBy(block, ast);
		    //如果方法有throws
		    if (hasThrows) {
			    SingleNameReference exRef = getNameReference("e", ast);
			    Statement rethrowStatement = new ThrowStatement(exRef, se, se);
			    setGeneratedBy(rethrowStatement, ast);
			    block.statements = new Statement[] { assignment, rethrowStatement };
		    } else {
			    block.statements = new Statement[] { assignment };
		    }
		    tryStatement.catchBlocks = new Block[] { block };

		    /************************ finally语句块 ****************************/
		    final Statement[] finallyBlock = new Statement[2];
		    /**
		     *  if (exception == null) {
		     *       transaction.setStatus(SUCCESS);
		     *  } else { 
		     *       transaction.setStatus(e); 
		     *  }
		     */
		    exception = getNameReference("exception", ast);
		    nullLiteral = new NullLiteral(pS, pE);
		    setGeneratedBy(nullLiteral, ast);
		    // 比较if (exception == null)
		    EqualExpression exceptionIsNull = new EqualExpression(exception, nullLiteral, OperatorIds.EQUAL_EQUAL);
		    setGeneratedBy(exceptionIsNull, ast);

		    long position = (long)ast.sourceStart << 32 | ast.sourceEnd;
		    MessageSend setSuccess = transactionSetStatussMethod(ast, pS, pE, position, true);
		    //if 语句, IfStatement表示如果exceptionIsNull表达式成立，则执行setSuccess语句
		    IfStatement ifExcaptionEqualsNull = new IfStatement(exceptionIsNull, setSuccess, pS, pE);
		    MessageSend setException = transactionSetStatussMethod(ast, pS, pE, position, false);
		    //else 语句, else语句单独有一个elseStatement
		    ifExcaptionEqualsNull.elseStatement = setException;
		    setGeneratedBy(ifExcaptionEqualsNull, ast);
		    /** 
		     * MessageSend是方法调用
		     * receiver方法表示调用对象
		     * 不同于selector，而selector表示最终调用的方法； 
		     * 例如System.out.println()方法中，System.out为receiver, 而println是selector
		     * arguments是方法调用参数
		     */
		    MessageSend transactionComplete = new MessageSend();
		    setGeneratedBy(transactionComplete, ast);
		    transactionComplete.sourceStart = ast.sourceStart;
		    transactionComplete.sourceEnd = ast.sourceEnd;
		    transactionComplete.nameSourcePosition = position;
		    transactionComplete.receiver = getNameReference("transaction", ast);
		    transactionComplete.selector = "complete".toCharArray();
		    finallyBlock[0] = ifExcaptionEqualsNull;
		    finallyBlock[1] = transactionComplete;
		    tryStatement.finallyBlock = new Block(0);
		    setGeneratedBy(tryStatement.finallyBlock, ast);
		    tryStatement.finallyBlock.statements = finallyBlock;

		    //替换原来AST树的节点，这是源码更改的关键
		    ((AbstractMethodDeclaration)blockNode).statements = newStatements;
		    owner.rebuild();
		    break;

		default:
		    annotationNode.addError("@EleTrace is legal only on types.");
		    break;
	     }
	}
	
	/**
	 * 获得变量引用
	 * @param name 变量名
	 * @param ast
	 * @return
	 */
	private SingleNameReference getNameReference(final String name, final Annotation ast) {
		SingleNameReference ref = new SingleNameReference(name.toCharArray(), 0);
		setGeneratedBy(ref, ast);
		return ref;
	}
	
	/**
	 * 判断该方法是否有throws异常
	 * @param blockNode
	 * @return
	 */
	private boolean checkIfHasThrows(final ASTNode blockNode) {
		//取得方法节点
		AbstractMethodDeclaration method = (AbstractMethodDeclaration)blockNode;
		TypeReference[] throwsref = method.thrownExceptions;
		//如果有throws语句
		if (throwsref != null && throwsref.length > 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * transaction.setStatus
	 * @param ast
	 * @param pS
	 * @param pE
	 * @param position
	 * @param isSuccess true为transaction.setStatus(SUCCESS), false为transaction.setStatus(exception)
	 * @return
	 */
	private MessageSend transactionSetStatussMethod(final Annotation ast, final int pS, final int pE,
	   	 final long position, final boolean isSuccess) {
		
	        MessageSend methodCall = new MessageSend();
	    	setGeneratedBy(methodCall, ast);
		SingleNameReference trans = getNameReference("transaction", ast);
		methodCall.sourceStart = pS;
		methodCall.sourceEnd = pE;
		trans = getNameReference("transaction", ast);
		methodCall.receiver = trans; //createNameReference("transaction", ast);
		methodCall.selector = "setStatus".toCharArray();

		if (isSuccess) {
			methodCall.arguments = new Expression[] { 
				new StringLiteral("0".toCharArray(), pS, pE, 0)  //0: SUCCESS
			};
		} else {
			SingleNameReference ex = getNameReference("exception", ast);
			methodCall.arguments = new Expression[] { ex };
		}
		methodCall.nameSourcePosition = position;
		return methodCall;
	}
	
	public LocalDeclaration createLocalDeclaration(ASTNode source, char[] dollarFieldName, 
			TypeReference type, Expression initializer) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		LocalDeclaration tempVar = new LocalDeclaration(dollarFieldName, pS, pE);
		setGeneratedBy(tempVar, source);
		tempVar.initialization = initializer;
		tempVar.type = type;
		tempVar.type.sourceStart = pS; tempVar.type.sourceEnd = pE;
		setGeneratedBy(tempVar.type, source);
		tempVar.modifiers = Modifier.FINAL;
		return tempVar;
	}
	
    	/**
    	 * 创建transaction局部变量，这里单独使用一个方法四因为transaction的初始值为一个方法调用。。。
    	 * @param ast
    	 * @param type
    	 * @param name
    	 * @return
    	 */
	private LocalDeclaration createTransactionField(final Annotation ast, final String type, final String name) {
		int pS = ast.sourceStart, pE = ast.sourceEnd;
		long p = (long) pS << 32 | pE;

		// Transaction transaction = Trace.newTransaction(pjp.getSignature().getDeclaringType().getName(), 
		//                                 pjp.getSignature().getName());
		LocalDeclaration valDecl = new LocalDeclaration("transaction".toCharArray(), pS, pE);
		setGeneratedBy(valDecl, ast);
		//	      valDecl.declarationSourceEnd = -1;
		valDecl.modifiers = Modifier.FINAL;
		valDecl.type = createTypeReference("me.ele.arch.etrace.common.modal.Transaction", ast);
		MessageSend methodCall = new MessageSend();
		setGeneratedBy(methodCall, ast);

		methodCall.receiver = createNameReference("me.ele.arch.etrace.agent.Trace", ast);
		methodCall.selector = "newTransaction".toCharArray();

		methodCall.arguments = new Expression[] { 
			new StringLiteral(type.toCharArray(), ast.sourceStart, ast.sourceEnd, 0), 
			    new StringLiteral(name.toCharArray(), ast.sourceStart, ast.sourceEnd, 0)
		};
		methodCall.nameSourcePosition = p;
		methodCall.sourceStart = pS;
		methodCall.sourceEnd = methodCall.statementEnd = pE;

		valDecl.initialization = methodCall;

		return valDecl;
	}

	 /**
	  * 得到一个类(以字符串形式，类似obj.getClass().getName())的类型信息
	  * @param typeName 类字符串(如obj.getClass().getName())
	  * @param ast
	  * @return
	  */
	private TypeReference createTypeReference(final String typeName, final Annotation ast) {
		int pS = ast.sourceStart, pE = ast.sourceEnd;
		long p = (long) pS << 32 | pE;

		TypeReference typeReference;
		if (typeName.contains(".")) {
			char[][] typeNameTokens = fromQualifiedName(typeName);
			long[] pos = new long[typeNameTokens.length];
			Arrays.fill(pos, p);

			typeReference = new QualifiedTypeReference(typeNameTokens, pos);
		} else {
			typeReference = null;
		}

		setGeneratedBy(typeReference, ast);
		return typeReference;
	}

	public TypeReference generateQualifiedTypeRef(final ASTNode source, char[]... varNames) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;

		TypeReference ref;

		long[] poss = Eclipse.poss(source, varNames.length);
		if (varNames.length > 1) ref = new QualifiedTypeReference(varNames, poss);
		else ref = new SingleTypeReference(varNames[0], p);
		setGeneratedBy(ref, source);
		return ref;
	}
	
}
