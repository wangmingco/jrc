package co.wangming.jrc;

import java.io.Serializable;

/**
 * HTTP请求返回对象
 * <p>
 * Created by congfei on 2017/8/25.
 */
public class Result<T> implements Serializable {

    private Integer code;

    private String msg;

    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public static Result success() {
        return success(null);
    }

    public static <R> Result success(R msg) {
        Result result = new Result();
        result.setCode(1);
        result.setMsg("成功");
        result.setData(msg);
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.setCode(2);
        result.setMsg(msg);
        return result;
    }

}
