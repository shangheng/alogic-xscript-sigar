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
 * 查找指定的进程信息
 * 
 * @author shangheng
 * 
 * @version 1.1.9.7 [20151104 duanyy] <br>
 * - 端口参数port改为可选，缺省为当前jvm所占用的端口 <br>
 */
public class HostProcess extends SigarOperation {

	protected String protocol = "tcp";
	protected String port = null;
	
	public HostProcess(String tag, Logiclet p) {
		super(tag, p);
	}

	@Override
	public void configure(Properties p){
		super.configure(p);
		tag = PropertiesConstants.getRaw(p, "tag", tag);
		type = PropertiesConstants.getRaw(p, "type", type);
		protocol =PropertiesConstants.getRaw(p, "protocol", protocol);
		port = p.GetValue("port",port,false, true);
	}

	@Override
	protected void onExecute(HostTools hostTools,Map<String, Object> root,
			final Map<String, Object> current, LogicletContext ctx,
			ExecuteWatcher watcher) {
			String tagValue = ctx.transform(tag);
			if (StringUtils.isEmpty(type)) {
				throw new BaseException("core.no_field", "type is not set,check your script.");
			}
			String _type = ctx.transform(type);
			String _protocol = ctx.transform(protocol);
			String _port = ctx.transform(port);
			Map<String,Object> host = new HashMap<String,Object>();
			
			HostTools.reportProcess(host, _protocol, _port);
	    
			if(_type.equals("process")){
				
				current.put(tagValue, host);
			}
	}	
	
}