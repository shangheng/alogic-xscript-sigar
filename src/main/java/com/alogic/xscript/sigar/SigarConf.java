package com.alogic.xscript.sigar;

import com.alogic.xscript.ExecuteWatcher;
import com.alogic.xscript.Logiclet;
import com.alogic.xscript.LogicletContext;
import com.alogic.xscript.doc.XsObject;
import com.alogic.xscript.plugins.Segment;
import com.alogic.xscript.sigar.util.HostTools;

public class SigarConf extends Segment{

	protected String cid = "$sigar";
	
	public SigarConf(String tag, Logiclet p) {
		super(tag, p);
		registerModule("sigar-cpu", HostCPU.class);
		registerModule("sigar-cpuInfo", HostCPUInfo.class);
		registerModule("sigar-os", HostOS.class);
		registerModule("sigar-fs", HostFileSystem.class);
		registerModule("sigar-info", HostInfo.class);
		registerModule("sigar-mem", HostMem.class);
		registerModule("sigar-netInfo", HostNetInfo.class);
		registerModule("sigar-netStat", HostNetStat.class);
		registerModule("sigar-process", HostProcess.class);
	}
	
	@Override
	protected void onExecute(XsObject root, 
			XsObject current, LogicletContext ctx, ExecuteWatcher watcher) {
		HostTools hostTool = new HostTools();
		try {
			ctx.setObject(cid, hostTool);
			super.onExecute(root, current, ctx, watcher);
		} finally {
			ctx.removeObject(cid);
		}
	}	
}
