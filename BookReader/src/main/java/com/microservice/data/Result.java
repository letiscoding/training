package com.microservice.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;

public class Result<T> implements Serializable {
	private Integer code;
	private String message;
	@JsonInclude(Include.NON_EMPTY)
	private T body;

	public Result(T body, ErrorCode code) {
		this.message = code.getMessage();
		this.code = code.getCode();
		this.body = body;
	}

	public Result(T body) {
		this(body, ErrorCode.SUCCESS);
	}

	public Result(ErrorCode code) {
		this(null, code);
	}

	/**
	 * 默认成功返回
	 */
	public Result() {
		this(null, ErrorCode.SUCCESS);
	}

	/**
	 * Returns the body of this entity.
	 */
	public T getBody() {
		return this.body;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
