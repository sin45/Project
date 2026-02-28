package com.starbucks.util;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 汉印云打印工具类（适配TP585打印机）
 * 文档地址 - https://developer.hprtcloud.com/#/integration/integrationIndex
 */
public class HprtCloudPrintUtil {
    // 汉印云API基础地址
    private static final String BASE_URL = "https://api.hprtcloud.com";
    // 替换为你的apiKey
    private static final String API_KEY = "你的汉印云apiKey";
    // 替换为你的secret
    private static final String SECRET = "你的汉印云secret";
    // 替换为你的TP585打印机SN码
    private static final String DEVICE_SN = "你的TP585打印机SN";

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private static final Gson GSON = new Gson();

    /**
     * 生成ESC/POS打印指令（适配TP585的58mm小票）
     * @param orderNo 订单号
     * @param amount 金额
     * @return ESC/POS指令字符串
     */
    private static String generateEscPosContent(String orderNo, String amount) {
        // 1. 初始化打印机
        byte[] initCmd = {0x1B, 0x40};
        // 2. 切纸指令（半切）
        byte[] cutPaperCmd = {0x1B, 0x69};
        // 3. 居中指令
        byte[] centerCmd = {0x1B, 0x61, 0x01};
        // 4. 取消居中指令
        byte[] cancelCenterCmd = {0x1B, 0x61, 0x00};
        // 5. 加粗指令
        byte[] boldOnCmd = {0x1B, 0x45, 0x01};
        // 6. 取消加粗指令
        byte[] boldOffCmd = {0x1B, 0x45, 0x00};

        // 拼接打印内容（编码为GBK，避免中文乱码）
        StringBuilder content = new StringBuilder();
        content.append(new String(initCmd, StandardCharsets.UTF_8));       // 初始化
        content.append(new String(centerCmd, StandardCharsets.UTF_8));    // 居中
        content.append(new String(boldOnCmd, StandardCharsets.UTF_8));    // 加粗
        content.append("测试店铺\n");                                   // 店铺名
        content.append(new String(boldOffCmd, StandardCharsets.UTF_8));   // 取消加粗
        content.append("----------------------------\n");
        content.append(new String(cancelCenterCmd, StandardCharsets.UTF_8)); // 取消居中
        content.append("订单号：").append(orderNo).append("\n");
        content.append("金额：¥").append(amount).append("\n");
        content.append("打印时间：").append(java.time.LocalDateTime.now()).append("\n");
        content.append("----------------------------\n");
        content.append(new String(centerCmd, StandardCharsets.UTF_8));    // 居中
        content.append("感谢惠顾，欢迎下次光临\n");
        content.append(new String(cutPaperCmd, StandardCharsets.UTF_8));   // 切纸

        return content.toString();
    }

    /**
     * 生成API签名（按汉印云规则）
     * @param params 参与签名的参数
     * @return 签名结果
     */
    private static String generateSign(Map<String, String> params) {
        // 签名规则：apiKey + secret + content + timestamp 拼接后MD5
        String signStr = params.get("apiKey") + SECRET + params.get("content") + params.get("timestamp");
        return DigestUtils.md5Hex(signStr).toUpperCase();
    }

    /**
     * 下发打印任务
     * @param orderNo 订单号
     * @param amount 金额
     * @return 打印任务ID
     * @throws IOException 网络异常
     */
    public static String submitPrintTask(String orderNo, String amount) throws IOException {
        // 1. 生成ESC/POS打印内容并Base64编码
        String escPosContent = generateEscPosContent(orderNo, amount);
        String base64Content = Base64.getEncoder().encodeToString(escPosContent.getBytes(StandardCharsets.UTF_8));

        // 2. 构造请求参数
        Map<String, String> params = new HashMap<>();
        params.put("apiKey", API_KEY);
        params.put("deviceSn", DEVICE_SN);
        params.put("content", base64Content);
        params.put("title", "订单打印-" + orderNo);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 生成签名
        params.put("sign", generateSign(params));

        // 3. 构建请求
        RequestBody requestBody = FormBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                "apiKey=" + params.get("apiKey") +
                        "&deviceSn=" + params.get("deviceSn") +
                        "&content=" + params.get("content") +
                        "&title=" + params.get("title") +
                        "&timestamp=" + params.get("timestamp") +
                        "&sign=" + params.get("sign"));

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/print")
                .post(requestBody)
                .build();

        // 4. 发送请求并解析响应
        Response response = OK_HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String responseStr = response.body().string();
            PrintResponse printResponse = GSON.fromJson(responseStr, PrintResponse.class);
            if ("0".equals(printResponse.getCode())) {
                System.out.println("打印任务下发成功，任务ID：" + printResponse.getData().getTaskId());
                return printResponse.getData().getTaskId();
            } else {
                throw new RuntimeException("打印任务下发失败：" + printResponse.getMsg());
            }
        } else {
            throw new RuntimeException("请求汉印云API失败，响应码：" + response.code());
        }
    }

    /**
     * 查询打印任务状态
     * @param taskId 打印任务ID
     * @return 任务状态（0：待打印 1：打印中 2：打印完成 3：打印失败 4：已取消）
     * @throws IOException 网络异常
     */
    public static String queryPrintTaskStatus(String taskId) throws IOException {
        // 1. 构造请求参数
        Map<String, String> params = new HashMap<>();
        params.put("apiKey", API_KEY);
        params.put("taskId", taskId);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 生成签名（规则：apiKey + secret + taskId + timestamp）
        String signStr = API_KEY + SECRET + taskId + params.get("timestamp");
        String sign = DigestUtils.md5Hex(signStr).toUpperCase();

        // 2. 构建请求
        String url = BASE_URL + "/api/print/status?apiKey=" + API_KEY +
                "&taskId=" + taskId +
                "&timestamp=" + params.get("timestamp") +
                "&sign=" + sign;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        // 3. 发送请求并解析响应
        Response response = OK_HTTP_CLIENT.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            String responseStr = response.body().string();
            TaskStatusResponse statusResponse = GSON.fromJson(responseStr, TaskStatusResponse.class);
            if ("0".equals(statusResponse.getCode())) {
                String status = statusResponse.getData().getStatus();
                String statusDesc = switch (status) {
                    case "0" -> "待打印";
                    case "1" -> "打印中";
                    case "2" -> "打印完成";
                    case "3" -> "打印失败";
                    case "4" -> "已取消";
                    default -> "未知状态";
                };
                System.out.println("任务状态：" + statusDesc + "（状态码：" + status + "）");
                return status;
            } else {
                throw new RuntimeException("查询任务状态失败：" + statusResponse.getMsg());
            }
        } else {
            throw new RuntimeException("请求汉印云API失败，响应码：" + response.code());
        }
    }

    // 打印任务响应实体类
    static class PrintResponse {
        private String code;      // 0：成功 非0：失败
        private String msg;       // 提示信息
        private PrintData data;   // 数据体

        // getter & setter
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public PrintData getData() { return data; }
        public void setData(PrintData data) { this.data = data; }

        static class PrintData {
            private String taskId; // 任务ID

            // getter & setter
            public String getTaskId() { return taskId; }
            public void setTaskId(String taskId) { this.taskId = taskId; }
        }
    }

    // 任务状态响应实体类
    static class TaskStatusResponse {
        private String code;          // 0：成功 非0：失败
        private String msg;           // 提示信息
        private TaskStatusData data;  // 数据体

        // getter & setter
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMsg() { return msg; }
        public void setMsg(String msg) { this.msg = msg; }
        public TaskStatusData getData() { return data; }
        public void setData(TaskStatusData data) { this.data = data; }

        static class TaskStatusData {
            private String status; // 任务状态码

            // getter & setter
            public String getStatus() { return status; }
            public void setStatus(String status) { this.status = status; }
        }
    }

    // 测试方法
    public static void main(String[] args) {
        try {
            // 1. 下发打印任务
            String taskId = submitPrintTask("TEST20260228001", "19.90");

            // 2. 模拟轮询查询任务状态（实际项目中可异步处理）
            Thread.sleep(2000);
            queryPrintTaskStatus(taskId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}