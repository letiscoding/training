package com.microservice.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

/**
 * Created by  错误代码汇总枚举 所有错误代码及其描述统一都在这个枚举上添加，统一管理，方便查看，联合使用
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

	// 成功
	SUCCESS(HttpStatus.OK.value(), "SUCCESS"),
	// 权限相关
	ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "ACCESS_DENIED"),
	// 数据不存在
	NOT_FOUND(HttpStatus.NOT_FOUND.value(), "NOT_FOUND"),
	// 参数错误
	BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"),
	// 缺少参数
	REQUIRE_ARGUMENT(HttpStatus.BAD_REQUEST.value(), "REQUIRE_ARGUMENT"),
	//参数不能为空
	REQUIRE_PARAMETERNOTNULL(HttpStatus.BAD_REQUEST.value(), "REQUIRE_PARAMETERNOTNULL"),
	// 非法参数
	INVALID_ARGUMENT(HttpStatus.BAD_REQUEST.value(), "INVALID_ARGUMENT"),
	// 角色不能为空
	EMPTY_ROLES_NOT_ALLOWED(HttpStatus.BAD_REQUEST.value(), "EMPTY_ROLES_NOT_ALLOWED"),
	// 服务不可用
	SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE.value(), "SERVICE_UNAVAILABLE");


	private int code;
	private String message;

	ErrorCode(int code, String message) {
		setCode(code);
		setMessage(message);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return String.valueOf(this.code);
	}

}
