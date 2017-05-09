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
 * 查询CPU使用情况
 * 
 * @author shangheng
 *
 */
public class HostNetStat extends SigarOperation {

	protected String name = null;
	
	public HostNetStat(String tag, Logiclet p) {
		super(tag, p);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void configure(Properties p) {
		super.configure(p);
		tag = PropertiesConstants.getRaw(p, "tag", tag);
		name = p.GetValue("name", name, false, true);
		type = PropertiesConstants.getRaw(p, "type", type);
	}

	@Override
	protected void onExecute(HostTools hostTools,Map<String, Object> root, final Map<String, Object> current, LogicletContext ctx,
			ExecuteWatcher watcher) {
		String tagValue = ctx.transform(tag);
		String _name = ctx.transform(name);

		if (StringUtils.isEmpty(type)) {
			throw new BaseException("core.no_field", "type is not set,check your script.");
		}
		String _type = ctx.transform(type);
		Map<String, Object> host = new HashMap<String, Object>();

		HostTools.reportNetStat(host, _name);
		if(_type.equals("netStat")){
		current.put(tagValue, host);
		}
	}

}