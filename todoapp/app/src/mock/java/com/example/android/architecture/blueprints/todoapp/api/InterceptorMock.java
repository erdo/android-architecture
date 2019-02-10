package com.example.android.architecture.blueprints.todoapp.api;

import com.example.android.architecture.blueprints.todoapp.App;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import co.early.fore.core.Affirm;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class InterceptorMock implements Interceptor {

    private final int httpResponseCode;
    private final String resourceFileName;

    public InterceptorMock(int httpResponseCode, String resourceFileName) {
        this.httpResponseCode = httpResponseCode;
        this.resourceFileName = resourceFileName;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        String bodyString = readFromFile(resourceFileName, Charset.forName("UTF-8"));

        return new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(httpResponseCode)
                .body(ResponseBody.create(MediaType.parse("application/json"), bodyString))
                .message("")
                .build();
    }

    public String readFromFile(final String fileName, final Charset charset) throws IOException{

        Affirm.notNull(fileName);
        Affirm.notNull(charset);

        try (InputStream is = App.inst().getAssets().open(fileName);
             InputStreamReader isw = new InputStreamReader(is, charset);
             BufferedReader br = new BufferedReader(isw)){

            String line;
            StringBuffer stringBuffer = new StringBuffer();

            while ((line = br.readLine()) != null) {
                stringBuffer.append(line);
            }

            return stringBuffer.toString();
        }
    }

}
