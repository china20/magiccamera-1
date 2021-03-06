package cn.cxw.magiccameralib.imagefilter;

import android.util.Log;

import cn.cxw.magiccameralib.opengldrawer.GeometricElement;

/**
 * Created by cxw on 2017/11/5.
 */

public class GPUImageBeautifyFilter extends GPUImageFilter {

    public static final String BILATERAL_FRAGMENT_SHADER = "" +
            "precision highp float;\n"+
            "   varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "    uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "    uniform highp vec2 singleStepOffset;\n" +
            "    uniform vec4 params;\n" +//外部传进来的参数，
            "    uniform highp float brightness;\n" + //要调整的明亮度。
            "\n" +
            "    const highp vec3 W = vec3(0.299, 0.587, 0.114);\n" +  //rgb转成灰度图公式。
            "    const highp mat3 saturateMatrix = mat3(\n" +  //计算饱合度公式
            "        1.1102, -0.0598, -0.061,\n" +
            "        -0.0774, 1.0826, -0.1186,\n" +
            "        -0.0228, -0.0228, 1.1772);\n" +
            "    highp vec2 blurCoordinates[24];\n" +//去噪模糊，卷积核。保存的是卷积核的各个坐标。
            "\n" +
            "    highp float hardLight(highp float color) {\n" +//高亮，突出噪点。
            "    if (color <= 0.5)\n" +
            "        color = color * color * 2.0;\n" +
            "    else\n" +
            "        color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n" +
            "    return color;\n" +
            "}\n" +
            "\n" +
            "    void main(){\n" +
            "    highp vec3 centralColor = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +//获取当前图片中的像素值。
            "    blurCoordinates[0] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -10.0);\n" +   //下面这些是填充卷积核的坐标。
            "    blurCoordinates[1] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 10.0);\n" +
            "    blurCoordinates[2] = textureCoordinate.xy + singleStepOffset * vec2(-10.0, 0.0);\n" +
            "    blurCoordinates[3] = textureCoordinate.xy + singleStepOffset * vec2(10.0, 0.0);\n" +
            "    blurCoordinates[4] = textureCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);\n" +
            "    blurCoordinates[5] = textureCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);\n" +
            "    blurCoordinates[6] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);\n" +
            "    blurCoordinates[7] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);\n" +
            "    blurCoordinates[8] = textureCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);\n" +
            "    blurCoordinates[9] = textureCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);\n" +
            "    blurCoordinates[10] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);\n" +
            "    blurCoordinates[11] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);\n" +
            "    blurCoordinates[12] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -6.0);\n" +
            "    blurCoordinates[13] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 6.0);\n" +
            "    blurCoordinates[14] = textureCoordinate.xy + singleStepOffset * vec2(6.0, 0.0);\n" +
            "    blurCoordinates[15] = textureCoordinate.xy + singleStepOffset * vec2(-6.0, 0.0);\n" +
            "    blurCoordinates[16] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);\n" +
            "    blurCoordinates[17] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);\n" +
            "    blurCoordinates[18] = textureCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);\n" +
            "    blurCoordinates[19] = textureCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);\n" +
            "    blurCoordinates[20] = textureCoordinate.xy + singleStepOffset * vec2(-2.0, -2.0);\n" +
            "    blurCoordinates[21] = textureCoordinate.xy + singleStepOffset * vec2(-2.0, 2.0);\n" +
            "    blurCoordinates[22] = textureCoordinate.xy + singleStepOffset * vec2(2.0, -2.0);\n" +
            "    blurCoordinates[23] = textureCoordinate.xy + singleStepOffset * vec2(2.0, 2.0);\n" +
            "\n" +
            "    highp float sampleColor = centralColor.g * 22.0;\n" + //取样本的绿通道的值。 旁边的22是卷积核在些坐标对应的权重。
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[0]).g;\n" + //下面的代码是就是卷积模糊的操作。
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[1]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[2]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[3]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[4]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[5]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[6]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[7]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[8]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[9]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[10]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[11]).g;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[12]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[13]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[14]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[15]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[16]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[17]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[18]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[19]).g * 2.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[20]).g * 3.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[21]).g * 3.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[22]).g * 3.0;\n" +
            "    sampleColor += texture2D(inputImageTexture, blurCoordinates[23]).g * 3.0;\n" +
            "\n" +
            "    sampleColor = sampleColor / 62.0;\n" +
            "\n" +
            "    highp float highPass = centralColor.g - sampleColor  + 0.5;\n" +  //计算出高反差保留值(英文名即highPass)。
            "\n" +
            "    for (int i = 0; i < 5; i++) {\n" +
            "        highPass = hardLight(highPass);\n" +
            "    }\n" +
            "    highp float lumance = dot(centralColor, W);\n" +  //获取当前未处理的像素值的明度。
            "\n" +
            "    highp float alpha = pow(lumance, params.r);\n" +  //通过传进来的param中的第一个通道值，来提升明度。此公式是伽马变换，(参数小于1的情况下)此变换提升整体亮度。
            "\n" +
            "    highp vec3 smoothColor = centralColor + (centralColor - vec3(highPass))*alpha*0.1;\n" +  //做下混合 x * (1 - a) + a * y = x + (y - x) * a
            "\n" +
            "    smoothColor.r = clamp(pow(smoothColor.r, params.g), 0.0, 1.0);\n" +  //提升整体的亮度。参数值越小提升的越多。
            "    smoothColor.g = clamp(pow(smoothColor.g, params.g), 0.0, 1.0);\n" +
            "    smoothColor.b = clamp(pow(smoothColor.b, params.g), 0.0, 1.0);\n" +
            "\n" +
            "    highp vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);\n" +  //以灰度值作为透明度将原图与混合后结果进行滤色、柔光等混合，并调节饱和度
            "    highp vec3 bianliang = max(smoothColor, centralColor);\n" +
            "    highp vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;\n" +
            "\n" +
            "    gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);\n" +
            "    gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);\n" +
            "    gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);\n" +
            "\n" +
            "    highp vec3 satcolor = gl_FragColor.rgb * saturateMatrix;\n" +  //计算饱合度。
            "    gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);\n" +  //根据参数将饱合度与当前颜色rgb值混合。
            "    gl_FragColor.rgb = vec3(gl_FragColor.rgb + vec3(brightness));\n" +
            "}";
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    private float mParams;

    public GPUImageBeautifyFilter() {
        super(NO_FILTER_VERTEX_SHADER, BILATERAL_FRAGMENT_SHADER);
    }

    @Override
    public void onInit() {
        super.onInit();
//        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(), "singleStepOffset");
//        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");

        mSingleStepOffsetLocation = mGlProgram.getUniformLocation("singleStepOffset");
        mParamsLocation = mGlProgram.getUniformLocation("params");

        setParams(0.4f);
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / width, 2.0f / height});
    }

    public void setParams(float params) {
        mParams = params;
//        float paramvec[] = {params, params, params, params};
        float paramvec[] = {0.33f, 0.33f, params, params};

        setFloatVec4(mParamsLocation, paramvec);
    }
    public void setROI(GeometricElement.Rect rect)
    {
        if (rect == null)
        {
            return ;
        }
        Log.d("GPUImageBeautifyFilter", "lt = x:" + rect.left + " y:" + rect.top + "  br = x:" + rect.right + " y:" + rect.bottom);
    }
}
