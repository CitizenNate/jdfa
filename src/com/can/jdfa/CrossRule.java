package com.can.jdfa;
public enum CrossRule{
	AND{
		@Override
		public boolean apply(boolean a,boolean b){
			return a&&b;
		}
	},
	OR{
		@Override
		public boolean apply(boolean a,boolean b){
			return a||b;
		}
	};
	public abstract boolean apply(boolean a,boolean b);
}
