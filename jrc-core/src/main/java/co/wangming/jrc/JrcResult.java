package co.wangming.jrc;

import java.io.Serializable;

/**
 * HTTP请求返回对象
 * <p>
 * Created by congfei on 2017/8/25.
 */
public class JrcResult<T> implements Serializable {

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
        return "JrcResult{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public static JrcResult success() {
        return success(null);
    }

    public static <R> JrcResult success(R msg) {
        JrcResult jrcResult = new JrcResult();
        jrcResult.setCode(1);
        jrcResult.setMsg("成功");
        jrcResult.setData(msg);
        return jrcResult;
    }

    public static JrcResult error(String msg) {
        JrcResult jrcResult = new JrcResult();
        jrcResult.setCode(2);
        jrcResult.setMsg(msg);
        return jrcResult;
    }

}
