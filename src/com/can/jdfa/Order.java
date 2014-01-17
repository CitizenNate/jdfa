package com.can.jdfa;
public enum Order{
	LESS{
		@Override
		public boolean applies(int a,int b){
			return a<b;
		}
	},
	EQUAL{
		@Override
		public boolean applies(int a,int b){
			return a==b;
		}
	},
	GREATER{
		@Override
		public boolean applies(int a,int b){
			return a>b;
		}
	};
	
	public abstract boolean applies(int a,int b);
}
