package nxt.http;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import nxt.Nxt;
import nxt.configuration.Setup;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

public class APICallGenerator {

    private static Set<String> ENTITY_IDENTIFIERS = new HashSet<>(Arrays.asList(
            "account", "recipient", "sender", "asset", "poll", "account", "currency", "order", "offer", "transaction",
            "ledgerId", "event", "goods", "buyer", "purchase", "holding", "block", "ecBlockId" ));

    private static Set<String> CHAIN_IDENTIFIERS = new HashSet<>(Arrays.asList(
            "chain", "exchange"));

    private static Set<String> INT_IDENTIFIERS = new HashSet<>(Arrays.asList(
            "height", "timestamp", "firstIndex", "lastIndex", "type", "subtype", "deadline", "ecBlockHeight"));

    private static Set<String> BOOLEAN_IDENTIFIERS = new HashSet<>(Arrays.asList(
            "executedOnly", "phased", "broadcast", "voucher" ));

    private static Set<String> REMOTE_ONLY_APIS = new HashSet<>(Arrays.asList(
            "eventRegister", "eventWait" ));

    public static void main(String[] args) {
        APICallGenerator generator = new APICallGenerator();
        generator.generate();
    }

    private void generate() {
        Properties properties = new Properties();
        properties.put("nxt.adminPassword", "password");
        Nxt.init(Setup.NOT_INITIALIZED, properties);
        APIServlet.initClass();
        Map<String, APIServlet.APIRequestHandler> apiRequestHandlers = APIServlet.getAPIRequestHandlers();
        for (String requestType : apiRequestHandlers.keySet()) {
            generateApiCall(requestType, apiRequestHandlers.get(requestType));
        }
    }

    private void generateApiCall(String requestType, APIServlet.APIRequestHandler apiRequestHandler) {
        String typeName = initialCaps(requestType) + "Call";
        ClassName className = ClassName.get("nxt.http.callers", typeName);
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(typeName).addModifiers(Modifier.PUBLIC)
                .superclass(ParameterizedTypeName.get(ClassName.get(APICall.Builder.class), className));

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addStatement("super($S)", requestType)
                .build();
        classBuilder.addMethod(constructor);

        MethodSpec.Builder factoryBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(className);
        if (apiRequestHandler.isChainSpecific()) {
            factoryBuilder.addStatement("$L instance = new $L()", typeName, typeName)
                .addStatement("instance.param(\"chain\", chain)")
                .addStatement("return instance")
                .addParameter(int.class, "chain");
        } else {
            factoryBuilder.addStatement("return new $L()", typeName);
        }
        classBuilder.addMethod(factoryBuilder.build());

        Map<String, Integer> paramMap = apiRequestHandler.getParameters().stream().collect(groupingBy(Function.identity(), summingInt(e -> 1)));
        for (String paramName : paramMap.keySet()) {
            if (ENTITY_IDENTIFIERS.contains(paramName)) {
                addMethod(className, classBuilder, paramMap, paramName, "param", String.class);
                addMethod(className, classBuilder, paramMap, paramName, "unsignedLongParam", long.class);
            } else if (CHAIN_IDENTIFIERS.contains(paramName)) {
                addMethod(className, classBuilder, paramMap, paramName, "param", String.class);
                addMethod(className, classBuilder, paramMap, paramName, "param", int.class);
            } else if (INT_IDENTIFIERS.contains(paramName)) {
                addMethod(className, classBuilder, paramMap, paramName, "param", int.class);
            } else if (BOOLEAN_IDENTIFIERS.contains(paramName) || paramName.startsWith("include") || paramName.startsWith("is") || paramName.contains("Is")) {
                addMethod(className, classBuilder, paramMap, paramName, "param", boolean.class);
            } else if (paramName.endsWith("NQT") || paramName.endsWith("FQT") || paramName.endsWith("FXT") || paramName.endsWith("QNT")) {
                addMethod(className, classBuilder, paramMap, paramName, "param", long.class);
            } else {
                addMethod(className, classBuilder, paramMap, paramName, "param", String.class);
            }
            if (paramName.startsWith("fullHash") || paramName.contains("FullHash") ||
                    paramName.startsWith("publicKey") || paramName.contains("PublicKey") ||
                    paramName.endsWith("MessageData") || paramName.endsWith("Nonce") ||
                    paramName.endsWith("ransactionBytes")) {
                addMethod(className, classBuilder, paramMap, paramName, "param", byte[].class);
            }
        }
        String fileParam = apiRequestHandler.getFileParameter();
        if (fileParam != null) {
            MethodSpec.Builder setterBuilder = MethodSpec.methodBuilder(fileParam)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(APICall.Builder.class)
                    .addParameter(byte[].class, "b")
                    .addStatement("return parts($S, $L)", fileParam, "b");
            MethodSpec setter = setterBuilder.build();
            classBuilder.addMethod(setter);
        }

        if (REMOTE_ONLY_APIS.contains(requestType)) {
            MethodSpec.Builder remoteOnlyBuilder = MethodSpec.methodBuilder("isRemoteOnly")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(boolean.class)
                    .addStatement("return true")
                    .addAnnotation(Override.class);
            MethodSpec remoteOnly = remoteOnlyBuilder.build();
            classBuilder.addMethod(remoteOnly);
        }

        TypeSpec typeSpec = classBuilder.build();
        JavaFile javaFile = JavaFile.builder("nxt.http.callers", typeSpec)
                .indent("    ")
                .addFileComment("Auto generated code, do not modify")
                .build();
        Path directory = Paths.get("./src/java");
        try {
            javaFile.writeTo(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMethod(ClassName className, TypeSpec.Builder classBuilder, Map<String, Integer> paramMap, String paramName, String paramMethodName, Class paramMethodType) {
        MethodSpec.Builder setterBuilder;
        setterBuilder = MethodSpec.methodBuilder(paramName)
                .addModifiers(Modifier.PUBLIC)
                .returns(className)
                .addStatement("return " + paramMethodName + "($S, $L)", paramName, paramName);
        if (paramMap.get(paramName) > 1) {
            Class arrayClass = Array.newInstance(paramMethodType, 0).getClass();
            setterBuilder.addParameter(arrayClass, paramName).varargs();
        } else {
            setterBuilder.addParameter(paramMethodType, paramName);
        }

        MethodSpec setter =  setterBuilder.build();
        classBuilder.addMethod(setter);
    }

    private String initialCaps(String requestType) {
        return requestType.substring(0, 1).toUpperCase() + requestType.substring(1);
    }
}
