package com.alogic.xscript.sigar;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alogic.xscript.AbstractLogiclet;
import com.alogic.xscript.ExecuteWatcher;
import com.alogic.xscript.Logiclet;
import com.alogic.xscript.LogicletContext;
import com.alogic.xscript.sigar.util.HostTools;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;

public abstract class SigarOperation extends AbstractLogiclet {
	protected String pid = "$sigar";
	
	/**
	 * 返回结果的id
	 */
	protected String id;
	
	/**
	 * 数据集
	 */
	protected String tag = "data";
	
	protected String type  = null;
	

	public SigarOperation(String tag, Logiclet p) {
		super(tag, p);
	}

	public void configure(Properties p) {
		super.configure(p);
		pid = PropertiesConstants.getString(p, "pid", pid, true);
		id = PropertiesConstants.getString(p, "id", "$" + getXmlTag(), true);
	}
	
	@Override
	protected void onExecute(Map<String, Object> root, Map<String, Object> current, LogicletContext ctx,
			ExecuteWatcher watcher) {

		HostTools hostTool = ctx.getObject(pid);
		if(hostTool == null) {
			throw new BaseException("core.no_hostTool", "It must in a sigar context, check your script.");
		}
		
		if(StringUtils.isNotEmpty(id)) {
			onExecute(hostTool, root, current, ctx, watcher);
		}
		
	}
	

	protected abstract void onExecute(HostTools hostTool, Map<String, Object> root,
			Map<String, Object> current, LogicletContext ctx,
			ExecuteWatcher watcher);

}
