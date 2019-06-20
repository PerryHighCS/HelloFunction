package run.myCode;

import com.amazonaws.services.lambda.invoke.LambdaFunction;

public interface HelloFunction {
    @LambdaFunction(functionName="HelloFunction")
    CompileResponse compileAndRun(CompileRequest req);
}