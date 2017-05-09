package com.alogic.xscript.sigar;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alogic.xscript.ExecuteWatcher;
import com.alogic.xscript.Logiclet;
import com.alogic.xscript.LogicletContext;
import com.alogic.xscript.sigar.util.HostTools;
import com.anysoft.util.BaseException;
import com.anysoft.util.Properties;
import com.anysoft.util.PropertiesConstants;

/**
 * 报告文件系统信息
 * 
 * @author shangheng
 *
 */
public class HostFileSystem extends SigarOperation {

	public HostFileSystem(String tag, Logiclet p) {
		super(tag, p);
	}

	@Override
	public void configure(Properties p) {
		super.configure(p);
		tag = PropertiesConstants.getRaw(p, "tag", tag);
		type = PropertiesConstants.getRaw(p, "type", type);
	}

	@Override
	protected void onExecute(HostTools hostTool,Map<String, Object> root, final Map<String, Object> current, LogicletContext ctx,
			ExecuteWatcher watcher) {
		String tagValue = ctx.transform(tag);
		if (StringUtils.isEmpty(type)) {
			throw new BaseException("core.no_field", "type is not set,check your script.");
		}
		String _type = ctx.transform(type);
		Map<String, Object> host = new HashMap<String, Object>();

		HostTools.reportFileSystem(host);

		if (_type.equals("fs")) {

			current.put(tagValue, host);
		}
	}

}