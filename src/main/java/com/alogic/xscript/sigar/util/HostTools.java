package com.alogic.xscript.sigar.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.OperatingSystem;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.ProcTime;
import org.hyperic.sigar.ResourceLimit;
import org.hyperic.sigar.SigarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主机相关工具
 * @author duanyy
 *
 * @version 1.1.9.18 [20160331 duanyy] <br>
 * - 用SigarCache来替代Sigar <br>
 * 
 * @version 1.1.10.17 [20160630 duanyy] <br>
 * - 在采集主机信息时，不再采集wholist <br>
 * 
 * @version 1.1.11.3 [20170201 duanyy] <br>
 * - 采用SLF4j日志框架输出日志 <br>
 */
public class HostTools {
	/**
	 * a logger of log4j
	 */
	protected static Logger logger = LoggerFactory.getLogger(HostTools.class);
	
	/**
	 * a sigar
	 */
	protected static SigarCache sigar= SigarCache.get();
	
	/**
	 * 报告指定的进场信息
	 * 
	 * @param output
	 * @param protocol
	 * @param port
	 */
	public static void reportProcess(Map<String,Object> output,String protocol,String port){
//		if (sigar.isWindows()){
//			return;
//		}
		try {
			output.put("protocol", protocol);
			output.put("port", port);
			
			/**
			 * 部分操作系统不支持此操作，会爆出异常
			 */
			long pid = sigar.getProcPort(protocol, port);
			
			output.put("pid", pid);
			{
				ProcState state = sigar.getProcState(pid);
				Map<String,Object> _state = new HashMap<String,Object>();
				
				_state.put("name", state.getName());
				_state.put("nice", state.getNice());
				_state.put("ppid", state.getPpid());
				_state.put("priority", state.getPriority());
				_state.put("processor", state.getProcessor());
				
				String stateDesc = "Run";
				switch (state.getState()){
					case ProcState.IDLE:
						stateDesc = "Idle";
						break;
					case ProcState.RUN:
						stateDesc = "Run";
						break;
					case ProcState.SLEEP:
						stateDesc = "Sleep";
						break;
					case ProcState.STOP:
						stateDesc = "Stop";
						break;
					case ProcState.ZOMBIE:
						stateDesc = "Zombie";
						break;
				}
				_state.put("state", stateDesc);
				
				_state.put("threads", state.getThreads());
				_state.put("tty", state.getTty());
				
				output.put("state", _state);
			}
			
			{
				ProcTime time = sigar.getProcTime(pid);
				
				Map<String,Object> _time = new HashMap<String,Object>();
				
				_time.put("start", time.getStartTime());
				_time.put("sys", time.getSys());
				_time.put("user", time.getUser());
				_time.put("total", time.getTotal());
				
				output.put("time", _time);
			}
			
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
			output.put("pid", -1);
		}
	}
	
	/**
	 * 报告网络统计信息
	 * 
	 * @param output 输出
	 * @param name 网卡名
	 */
	public static void reportNetStat(Map<String,Object> output,String name){
		try {
			output.put("name", name);
			//取网卡信息
			NetInterfaceConfig ifConfig = sigar.getNetInterfaceConfig(name);
			
			output.put("address", ifConfig.getAddress());
			output.put("broadcast", ifConfig.getBroadcast());
			output.put("desc", ifConfig.getDescription());
			output.put("destination", ifConfig.getDestination());
			output.put("flags", ifConfig.getFlags());
			output.put("hwaddr", ifConfig.getHwaddr());
			output.put("metric", ifConfig.getMetric());
			output.put("mtu", ifConfig.getMtu());
			output.put("name", ifConfig.getName());
			output.put("netmask", ifConfig.getNetmask());
			output.put("type", ifConfig.getType());

			{
				Map<String,Object> _netstat = new HashMap<String,Object>();
	
				NetInterfaceStat netstat = sigar.getNetInterfaceStat(name);
				_netstat.put("rx_bytes", netstat.getRxBytes());
				_netstat.put("rx_dropped", netstat.getRxDropped());
				_netstat.put("rx_errors", netstat.getRxErrors());
				_netstat.put("rx_frame", netstat.getRxFrame());
				_netstat.put("rx_overruns", netstat.getRxOverruns());
				_netstat.put("rx_packages", netstat.getRxPackets());
				
				_netstat.put("speed", netstat.getSpeed());
				
				_netstat.put("tx_bytes", netstat.getTxBytes());
				_netstat.put("tx_carrier", netstat.getTxCarrier());
				_netstat.put("tx_collisions", netstat.getTxCollisions());
				_netstat.put("tx_dropped", netstat.getTxDropped());
				_netstat.put("tx_errors", netstat.getTxErrors());
				_netstat.put("tx_overruns", netstat.getTxOverruns());
				_netstat.put("tx_packets", netstat.getTxPackets());
				
				output.put("stat", _netstat);
			}
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
		}			
	}
	
	/**
	 * 报告网络配置信息
	 * 
	 * @param output 输出
	 */
	public static void reportNetInfo(Map<String,Object> output){
		try {
			String fqdn = sigar.getFQDN();
			output.put("fqdn", fqdn);
			
			{
				NetInfo netInfo = sigar.getNetInfo();
				
				Map<String,Object> _netinfo = new HashMap<String,Object>();
				
				_netinfo.put("host_name", netInfo.getHostName());
				_netinfo.put("default_gate_way", netInfo.getDefaultGateway());
				_netinfo.put("domain_name", netInfo.getDomainName());
				_netinfo.put("primary_dns", netInfo.getPrimaryDns());
				_netinfo.put("secondary_dns", netInfo.getSecondaryDns());
				
				
				{
					String [] itfs = sigar.getNetInterfaceList();
					
					if (itfs.length > 0){
						List<Object> _ifConfigs = new ArrayList<Object>();
					
						for (String itfname:itfs){
							_ifConfigs.add(itfname);
						}						
						_netinfo.put("ifconfig", _ifConfigs);
					}
				}
				
				output.put("netInfo", _netinfo);
			}
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
		}	
	}
	
	public static void reportInfo(Map<String, Object> output) {
		OperatingSystem os = OperatingSystem.getInstance();

		output.put("os.name", os.getName());
		output.put("os.version", os.getVersion());
		output.put("os.arch", os.getArch());
		output.put("os.dataModel", os.getDataModel());
		output.put("os.desc", os.getDescription());
		output.put("os.machine", os.getMachine());
		output.put("os.endian", os.getCpuEndian());
		output.put("os.patchLevel", os.getPatchLevel());
		output.put("os.vendor", os.getVendor());
		output.put("os.vendorCodeName", os.getVendorCodeName());
		output.put("os.vendorName", os.getVendorName());
		output.put("os.vendorVersion", os.getVendorVersion());

		String hostname = System.getenv("KETTY_HOST");
		if (StringUtils.isNotEmpty(hostname)) {
			output.put("host.name", hostname);
		}

		SigarCache sigar = SigarCache.get();
		try {
			Mem mem = sigar.getMem();
			output.put("mem", mem.getTotal());
			output.put("mem.usage", mem.getUsedPercent());

			CpuPerc cpu = sigar.getCpuPerc();
			output.put("cpu.usage", cpu.getUser());

			output.put("rate.tx", sigar.getTxRate());
			output.put("rate.rx", sigar.getRxRate());

			// 因sigar的getSwap超级不稳定，暂时不提供swap分区信息
			// Swap swap = sigar.getSwap();
			// meas.set("swap", swap.getTotal());
			output.put("swap", 0);
			CpuInfo[] cpuInfo = sigar.getCpuInfoList();
			output.put("cpu.vcores", cpuInfo.length);
			if (cpuInfo.length > 0) {
				output.put("cpu.mhz", cpuInfo[0].getMhz());
				output.put("cpu.cache", cpuInfo[0].getCacheSize());
				output.put("cpu.vendor", cpuInfo[0].getVendor());
				output.put("cpu.model", cpuInfo[0].getModel());
				output.put("cpu.totalSockets", cpuInfo[0].getTotalSockets());
				output.put("cpu.totalCores", cpuInfo[0].getTotalCores());
				output.put("cpu.coresPerSocket", cpuInfo[0].getCoresPerSocket());
			}

		} catch (SigarException e) {

		}
	}
	
	/**
	 * 报告操作系统信息
	 * @param output 输出
	 */
	public static void reportOS(Map<String,Object> output){
		try {
			String fqdn = sigar.getFQDN();
			output.put("fqdn", fqdn);
			
			{
				Map<String,Object> _os = new HashMap<String,Object>();
				OperatingSystem os = OperatingSystem.getInstance();
				
				_os.put("name", os.getName());
				_os.put("version", os.getVersion());				
				_os.put("arch", os.getArch());
				_os.put("data_model", os.getDataModel());
				_os.put("desc", os.getDescription());
				_os.put("machine", os.getMachine());
				_os.put("endian", os.getCpuEndian());
				_os.put("patch_level", os.getPatchLevel());
				
				_os.put("vendor", os.getVendor());
				_os.put("vendor_code_name", os.getVendorCodeName());
				_os.put("vendor_name", os.getVendorName());
				_os.put("vendor_version", os.getVendorVersion());
				
				// who
				/*
				 * 在docker下不支持wholist，启用
				{
					Who [] whoList = sigar.getWhoList();
					if (whoList.length > 0){
						List<Object> _whos = new ArrayList<Object>();
						
						for (Who who:whoList){
							Map<String,Object> _who = new HashMap<String,Object>();
							
							_who.put("user", who.getUser());
							_who.put("device", who.getDevice());
							_who.put("login_time", who.getTime());
							_who.put("host", who.getHost());
							
							_whos.add(_who);
						}
						
						_os.put("who", _whos);
					}
				}
				*/
				//limits
				{
					ResourceLimit limits = sigar.getResourceLimit();
					
					Map<String,Object> _limits = new HashMap<String,Object>();
					
					_limits.put("core_cur", limits.getCoreCur());
					_limits.put("core_max", limits.getCoreMax());
					_limits.put("cpu_cur", limits.getCpuCur());
					_limits.put("cpu_max", limits.getCpuMax());
					_limits.put("data_cur", limits.getDataCur());
					_limits.put("data_max", limits.getDataMax());
					_limits.put("file_size_cur", limits.getFileSizeCur());
					_limits.put("file_size_max", limits.getFileSizeMax());
					_limits.put("mem_cur", limits.getMemoryCur());
					_limits.put("mem_max", limits.getMemoryMax());
					_limits.put("open_files_cur", limits.getOpenFilesCur());
					_limits.put("open_files_max", limits.getOpenFilesMax());
					_limits.put("pipe_size_cur", limits.getPipeSizeCur());
					_limits.put("pipe_size_max", limits.getPipeSizeMax());
					_limits.put("processes_cur", limits.getProcessesCur());
					_limits.put("processes_max", limits.getProcessesMax());
					_limits.put("stack_cur", limits.getStackCur());
					_limits.put("stack_max", limits.getStackMax());
					_limits.put("vmem_cur", limits.getVirtualMemoryCur());
					_limits.put("vmem_max", limits.getVirtualMemoryMax());
					
					_os.put("limits", _limits);
				}
				
				output.put("os", _os);
			}
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
		}		
	}

	/**
	 * 报告文件系统的信息
	 * @param output 输出
	 */
	public static void reportFileSystem(Map<String,Object> output){
		try {
			String fqdn = sigar.getFQDN();
			output.put("fqdn", fqdn);
			
			FileSystem[] fsList = sigar.getFileSystemList();
			
			if (fsList.length > 0)
			{
				List<Object> _fss = new ArrayList<Object>();
				
				for (FileSystem fs:fsList){
					Map<String,Object> _fs = new HashMap<String,Object>();
					
					_fs.put("device", fs.getDevName());
					_fs.put("dir", fs.getDirName());
					_fs.put("flags", fs.getFlags());
					_fs.put("options", fs.getOptions());
					_fs.put("sys_type_name", fs.getSysTypeName());
					_fs.put("type", fs.getType());
					_fs.put("type_name", fs.getTypeName());
					
					try {
						FileSystemUsage usage = sigar.getMountedFileSystemUsage(fs.getDirName());
						
						Map<String,Object> _usage = new HashMap<String,Object>();
						
						_usage.put("total", usage.getTotal());
						_usage.put("used", usage.getUsed());
						_usage.put("used_percent", usage.getUsePercent());
						_usage.put("avail", usage.getAvail());
						_usage.put("free", usage.getFree());
						
						_usage.put("read_bytes", usage.getDiskReadBytes());
						_usage.put("reads", usage.getDiskReads());
						_usage.put("write_bytes", usage.getDiskWriteBytes());
						_usage.put("writes", usage.getDiskWrites());
						
						_fs.put("status", "mounted");
						_fs.put("usage", _usage);
					}catch (Exception ex){
						_fs.put("status", "unmouted");
					}
					
					_fss.add(_fs);
				}
				
				output.put("fs", _fss);
			}
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
		}
	}
	/**
	 * 报告内存及SWAP分区的使用情况
	 * @param output 输出
	 */
	public static void reportMem(Map<String,Object> output){
		try {
			String fqdn = sigar.getFQDN();
			output.put("fqdn", fqdn);
			
			{
				Map<String,Object> _mem = new HashMap<String,Object>();
				Mem mem = sigar.getMem();
				
				_mem.put("actual_free", mem.getActualFree());
				_mem.put("actual_used", mem.getActualUsed());
				_mem.put("free", mem.getFree());
				_mem.put("free_percent", mem.getFreePercent());
				_mem.put("ram", mem.getRam());
				_mem.put("total", mem.getTotal());
				_mem.put("used", mem.getUsed());
				_mem.put("used_percent", mem.getUsedPercent());
				
				output.put("mem", _mem);
			}
			{
				Map<String,Object> _swap = new HashMap<String,Object>();
				//因sigar的getSwap超级不稳定，暂时不提供swap分区信息
				//Swap swap = sigar.getSwap();
				//_swap.put("free", swap.getFree());
				//_swap.put("page_in", swap.getPageIn());
				//_swap.put("page_out", swap.getPageOut());
				//_swap.put("total", swap.getTotal());
				//_swap.put("used", swap.getUsed());
				
				_swap.put("free", 0);
				_swap.put("page_in",0);
				_swap.put("page_out", 0);
				_swap.put("total",0);
				_swap.put("used", 0);				
				output.put("swap", _swap);
			}
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
		}
	}
	
	/**
	 * 报告CPU使用情况
	 * @param output 输出
	 */
	public static void reportCPU(Map<String,Object> output){
		try {
			String fqdn = sigar.getFQDN();
			output.put("fqdn", fqdn);
			
			Map<String,Object> _cpu = new HashMap<String,Object>();
			//平均情况
			reportCPU(_cpu,sigar.getCpuPerc());
			
			CpuPerc [] detail = sigar.getCpuPercList();
			if (detail.length > 0){
				List<Object> _detail = new ArrayList<Object>();
				
				for (CpuPerc perc:detail){
					Map<String,Object> _item = new HashMap<String,Object>();
					reportCPU(_item,perc);
					_detail.add(_item);
				}
				
				_cpu.put("detail", _detail);
			}
			
			output.put("cpu", _cpu);
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
		}
		
	}
	
	protected static void reportCPU(Map<String,Object> output,CpuPerc perc){
		output.put("combined", CpuPerc.format(perc.getCombined()));
		output.put("idle", CpuPerc.format(perc.getIdle()));
		output.put("irq", CpuPerc.format(perc.getIrq()));
		output.put("nice", CpuPerc.format(perc.getNice()));
		output.put("soft_irq", CpuPerc.format(perc.getSoftIrq()));
		output.put("stolen", CpuPerc.format(perc.getStolen()));
		output.put("sys", CpuPerc.format(perc.getSys()));
		output.put("user", CpuPerc.format(perc.getUser()));
		output.put("wait", CpuPerc.format(perc.getWait()));
	}
	
	/**
	 * 报告CPU信息
	 * @param output 输出
	 */
	public static void reportCPUInfo(Map<String,Object> output) {
		try {
			String fqdn = sigar.getFQDN();
			output.put("fqdn", fqdn);
			
			CpuInfo [] cpus = sigar.getCpuInfoList();
			if (cpus.length > 0) {
				Map<String, Object> _info = new HashMap<String, Object>();
				
				_info.put("vcores", cpus.length);
				_info.put("cache_size", cpus[0].getCacheSize());
				_info.put("cores_per_socket", cpus[0].getCoresPerSocket());
				_info.put("mhz", cpus[0].getMhz());
				_info.put("model", cpus[0].getModel());
				_info.put("total_cores", cpus[0].getCoresPerSocket() * cpus[0].getTotalSockets());
				_info.put("total_sockets", cpus[0].getTotalSockets());
				_info.put("vendor", cpus[0].getVendor());

				output.put("cpu_info", _info);
			}
			
		} catch (SigarException e) {
			logger.error("Failed to gather info with sigar",e);
		}
	}
	
	public static void main(String [] args){
		try {
			OperatingSystem os = OperatingSystem.getInstance();
			
			System.out.println(os.getArch());
			System.out.println(os.getCpuEndian());
			System.out.println(os.getDataModel());
			System.out.println(os.getDescription());
			System.out.println(os.getMachine());
			String fqdn = sigar.getFQDN();
			System.out.println(fqdn);
			
		} catch (Exception e) {
			logger.error("Failed to gather info with sigar",e);
		}
	}
} 
