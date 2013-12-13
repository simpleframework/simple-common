package net.simpleframework.ado.trans;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class TransactionObjectCallback<T> implements ITransactionCallback<T> {

	@Override
	public abstract T onTransactionCallback() throws Throwable;
}
