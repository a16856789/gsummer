package cn.edu.zucc.common

import jodd.io.findfile.ClassScanner;
import jodd.io.findfile.ClassFinder.EntryData;

class PackageScanner {
	static def scan(String pg, Closure closure) {
		ClassScanner cs = new ClassScanner() {
			@Override
			protected void onEntry(EntryData entryData) throws Exception {
				def clazz = PackageScanner.class.getClassLoader().loadClass("${pg}.${entryData.name}")
				closure(clazz)
			}
		}
		cs.scan(PackageScanner.class.getResource("/" + pg.replace('.', '/')))
		// cs.scan("/" + pg.replace('.', '/'))
	}
	
	public static void main(String[] args) {
		scan("jodd.bean") {
			println it
		}
	}
}
