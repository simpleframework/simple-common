package net.simpleframework.ado.trans;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public abstract class TransactionVoidCallback extends TransactionObjectCallback<Object> {

	@Override
	public Object onTransactionCallback() throws Throwable {
		doTransactionVoidCallback();
		return null;
	}

	protected abstract void doTransactionVoidCallback() throws Throwable;
}
