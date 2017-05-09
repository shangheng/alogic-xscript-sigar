package com.alogic.xscript.sigar.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.DirStat;
import org.hyperic.sigar.DirUsage;
import org.hyperic.sigar.DiskUsage;
import org.hyperic.sigar.FileInfo;
import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.FileSystemMap;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.MultiProcCpu;
import org.hyperic.sigar.NetConnection;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NetRoute;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.NfsClientV2;
import org.hyperic.sigar.NfsClientV3;
import org.hyperic.sigar.NfsServerV2;
import org.hyperic.sigar.NfsServerV3;
import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.ProcCred;
import org.hyperic.sigar.ProcCredName;
import org.hyperic.sigar.ProcExe;
import org.hyperic.sigar.ProcFd;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.ProcStat;
import org.hyperic.sigar.ProcState;
import org.hyperic.sigar.ProcTime;
import org.hyperic.sigar.ResourceLimit;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;
import org.hyperic.sigar.Swap;
import org.hyperic.sigar.Tcp;
import org.hyperic.sigar.Uptime;
import org.hyperic.sigar.Who;

/**
 * Sigar缓存
 * 
 * 提供和Sigar一样的接口，将Sigar的结果缓存一段时间
 * 
 * @author duanyy
 * 
 * @version 1.1.9.18 [20160331 duanyy] <br>
 * - 增加SigarCache来替代Sigar，避免短时间重复调用Sigar <br>
 *
 */
public class SigarCache implements SigarProxy{
	private Sigar sigar = null;
	private ConcurrentHashMap<String,SigarData> cached = null;
	private Rate txRate = new Rate();
	private Rate rxRate = new Rate();
	private boolean isWindows = false;
	
	private SigarCache(){
		sigar = new Sigar();
		cached = new ConcurrentHashMap<String,SigarData>();
		isWindows = System.getProperty("os.name").toLowerCase().startsWith("win") ? true:false;
	}
	
	private static SigarCache instance = null;
	
	public static SigarCache get(){
		if (instance == null){
			synchronized (SigarCache.class){
				if (instance == null){
					instance = new SigarCache();
				}
			}
		}
		return instance;
	}
	
	public boolean isWindows(){
		return isWindows;
	}
	
	/**
	 * 获取当前bond0网卡的tx速率
	 * @return tx速率
	 * @throws SigarException
	 */
	public synchronized double getTxRate() throws SigarException{
		if (isWindows) return 0.0f;
		NetInterfaceStat netstat = sigar.getNetInterfaceStat("bond0");
		if (netstat == null){
			return 0.0f;
		}
		long tx = netstat.getTxBytes();
		return txRate.getRate(tx);
	}
	
	/**
	 * 获取当前bond0网卡的rx速率
	 * @return rx速率
	 * @throws SigarException
	 */
	public synchronized double getRxRate() throws SigarException{
		if (isWindows) return 0.0f;
		NetInterfaceStat netstat = sigar.getNetInterfaceStat("bond0");
		if (netstat == null){
			return 0.0f;
		}
		long tx = netstat.getRxBytes();
		return rxRate.getRate(tx);
	}	
	
	@Override
	public synchronized Cpu getCpu() throws SigarException {
		//对于CPU使用率信息，缓存10s
		long ttl = 10000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getCpu");
		if (found == null || found.isExpired(now)){
			Cpu data = sigar.getCpu();
			cached.put("getCpu", new SigarData(now + ttl,data));
			return data;
		}
		return (Cpu)found.data();
	}

	@Override
	public synchronized CpuInfo[] getCpuInfoList() throws SigarException {
		//对于CPU基础信息，缓存24小时
		long ttl = 24 * 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getCpuInfoList");
		if (found == null || found.isExpired(now)){
			CpuInfo[] data = sigar.getCpuInfoList();
			cached.put("getCpuInfoList", new SigarData(now + ttl,data));
			return data;
		}
		return (CpuInfo[]) found.data();
	}

	@Override
	public synchronized Cpu[] getCpuList() throws SigarException {
		//对于CPU使用率信息，缓存10s
		long ttl = 10000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getCpuList");
		if (found == null || found.isExpired(now)){
			Cpu[] data = sigar.getCpuList();
			cached.put("getCpuList", new SigarData(now + ttl,data));
			return data;
		}
		return (Cpu[])found.data();
	}

	@Override
	public synchronized CpuPerc getCpuPerc() throws SigarException {
		//对于CPU使用率信息，缓存10s
		long ttl = 10000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getCpuPerc");
		if (found == null || found.isExpired(now)){
			CpuPerc data = sigar.getCpuPerc();
			cached.put("getCpuPerc", new SigarData(now + ttl,data));
			return data;
		}
		return (CpuPerc)found.data();
	}

	@Override
	public synchronized CpuPerc[] getCpuPercList() throws SigarException {
		//对于CPU使用率信息，缓存10s
		long ttl = 10000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getCpuPercList");
		if (found == null || found.isExpired(now)){
			CpuPerc[] data = sigar.getCpuPercList();
			cached.put("getCpuPercList", new SigarData(now + ttl,data));
			return data;
		}
		return (CpuPerc[])found.data();
	}

	@Override
	public synchronized DirStat getDirStat(String arg0) throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getDirStat" + arg0);
		if (found == null || found.isExpired(now)){
			DirStat data = sigar.getDirStat(arg0);
			cached.put("getDirStat"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (DirStat)found.data();
	}

	@Override
	public synchronized DirUsage getDirUsage(String arg0) throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getDirUsage"+ arg0);
		if (found == null || found.isExpired(now)){
			DirUsage data = sigar.getDirUsage(arg0);
			cached.put("getDirUsage"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (DirUsage)found.data();
	}

	@Override
	public synchronized DiskUsage getDiskUsage(String arg0) throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getDiskUsage"+ arg0);
		if (found == null || found.isExpired(now)){
			DiskUsage data = sigar.getDiskUsage(arg0);
			cached.put("getDiskUsage"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (DiskUsage)found.data();
	}

	@Override
	public String getFQDN() throws SigarException {
		return sigar.getFQDN();
	}

	@Override
	public FileInfo getFileInfo(String arg0) throws SigarException {
		return sigar.getFileInfo(arg0);
	}

	@Override
	public synchronized FileSystem[] getFileSystemList() throws SigarException {
		//缓存24h
		long ttl = 24 * 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getFileSystemList");
		if (found == null || found.isExpired(now)){
			FileSystem[] data = sigar.getFileSystemList();
			cached.put("getFileSystemList", new SigarData(now + ttl,data));
			return data;
		}
		return (FileSystem[])found.data();
	}

	@Override
	public FileSystemMap getFileSystemMap() throws SigarException {
		return sigar.getFileSystemMap();
	}

	@Override
	public synchronized FileSystemUsage getFileSystemUsage(String arg0) throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getFileSystemList"+ arg0);
		if (found == null || found.isExpired(now)){
			FileSystemUsage data = sigar.getFileSystemUsage(arg0);
			cached.put("getFileSystemList"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (FileSystemUsage)found.data();
	}

	@Override
	public FileInfo getLinkInfo(String arg0) throws SigarException {
		return sigar.getLinkInfo(arg0);
	}

	@Override
	public synchronized double[] getLoadAverage() throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getLoadAverage");
		if (found == null || found.isExpired(now)){
			double[] data = sigar.getLoadAverage();
			cached.put("getLoadAverage", new SigarData(now + ttl,data));
			return data;
		}
		return (double[])found.data();
	}

	@Override
	public synchronized Mem getMem() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getMem");
		if (found == null || found.isExpired(now)){
			Mem data = sigar.getMem();
			cached.put("getMem", new SigarData(now + ttl,data));
			return data;
		}
		return (Mem)found.data();
	}

	@Override
	public synchronized FileSystemUsage getMountedFileSystemUsage(String arg0) throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getMountedFileSystemUsage"+ arg0);
		if (found == null || found.isExpired(now)){
			FileSystemUsage data = sigar.getMountedFileSystemUsage(arg0);
			cached.put("getMountedFileSystemUsage"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (FileSystemUsage)found.data();
	}

	@Override
	public synchronized MultiProcCpu getMultiProcCpu(String arg0) throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getMultiProcCpu"+ arg0);
		if (found == null || found.isExpired(now)){
			MultiProcCpu data = sigar.getMultiProcCpu(arg0);
			cached.put("getMultiProcCpu"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (MultiProcCpu)found.data();
	}

	@Override
	public synchronized ProcMem getMultiProcMem(String arg0) throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getMultiProcMem"+ arg0);
		if (found == null || found.isExpired(now)){
			ProcMem data = sigar.getMultiProcMem(arg0);
			cached.put("getMultiProcMem"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (ProcMem)found.data();
	}

	@Override
	public synchronized NetConnection[] getNetConnectionList(int arg0) throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetConnectionList"+ arg0);
		if (found == null || found.isExpired(now)){
			NetConnection[] data = sigar.getNetConnectionList(arg0);
			cached.put("getNetConnectionList"+ arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (NetConnection[])found.data();
	}

	@Override
	public synchronized NetInfo getNetInfo() throws SigarException {
		//缓存1h
		long ttl = 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetInfo");
		if (found == null || found.isExpired(now)){
			NetInfo data = sigar.getNetInfo();
			cached.put("getNetInfo", new SigarData(now + ttl,data));
			return data;
		}
		return (NetInfo)found.data();
	}

	@Override
	public synchronized NetInterfaceConfig getNetInterfaceConfig() throws SigarException {
		//缓存1h
		long ttl = 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetInterfaceConfig");
		if (found == null || found.isExpired(now)){
			NetInterfaceConfig data = sigar.getNetInterfaceConfig();
			cached.put("getNetInterfaceConfig", new SigarData(now + ttl,data));
			return data;
		}
		return (NetInterfaceConfig)found.data();
	}

	@Override
	public synchronized NetInterfaceConfig getNetInterfaceConfig(String arg0) throws SigarException {
		//缓存1h
		long ttl = 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetInterfaceConfig" + arg0);
		if (found == null || found.isExpired(now)){
			NetInterfaceConfig data = sigar.getNetInterfaceConfig(arg0);
			cached.put("getNetInterfaceConfig" + arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (NetInterfaceConfig)found.data();
	}

	@Override
	public synchronized String[] getNetInterfaceList() throws SigarException {
		//缓存1h
		long ttl = 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetInterfaceList");
		if (found == null || found.isExpired(now)){
			String[] data = sigar.getNetInterfaceList();
			cached.put("getNetInterfaceList", new SigarData(now + ttl,data));
			return data;
		}
		return (String[])found.data();		
	}

	@Override
	public synchronized NetInterfaceStat getNetInterfaceStat(String arg0) throws SigarException {
		//缓存10s
		long ttl =  10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetInterfaceStat" + arg0);
		if (found == null || found.isExpired(now)){
			NetInterfaceStat data = sigar.getNetInterfaceStat(arg0);
			cached.put("getNetInterfaceStat" + arg0, new SigarData(now + ttl,data));
			return data;
		}
		return (NetInterfaceStat)found.data();
	}

	@Override
	public String getNetListenAddress(long arg0) throws SigarException {
		return sigar.getNetListenAddress(arg0);
	}

	@Override
	public String getNetListenAddress(String arg0) throws SigarException {
		return sigar.getNetListenAddress(arg0);
	}

	@Override
	public synchronized NetRoute[] getNetRouteList() throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetRouteList");
		if (found == null || found.isExpired(now)){
			NetRoute[] data = sigar.getNetRouteList();
			cached.put("getNetRouteList", new SigarData(now + ttl,data));
			return data;
		}
		return (NetRoute[])found.data();	
	}

	@Override
	public String getNetServicesName(int arg0, long arg1) {
		return sigar.getNetServicesName(arg0, arg1);
	}

	@Override
	public synchronized NetStat getNetStat() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNetStat");
		if (found == null || found.isExpired(now)){
			NetStat data = sigar.getNetStat();
			cached.put("getNetStat", new SigarData(now + ttl,data));
			return data;
		}
		return (NetStat)found.data();
	}

	@Override
	public NfsClientV2 getNfsClientV2() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNfsClientV2");
		if (found == null || found.isExpired(now)){
			NfsClientV2 data = sigar.getNfsClientV2();
			cached.put("getNfsClientV2", new SigarData(now + ttl,data));
			return data;
		}
		return (NfsClientV2)found.data();
	}

	@Override
	public NfsClientV3 getNfsClientV3() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNfsClientV3");
		if (found == null || found.isExpired(now)){
			NfsClientV3 data = sigar.getNfsClientV3();
			cached.put("getNfsClientV3", new SigarData(now + ttl,data));
			return data;
		}
		return (NfsClientV3)found.data();
	}

	@Override
	public NfsServerV2 getNfsServerV2() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNfsServerV2");
		if (found == null || found.isExpired(now)){
			NfsServerV2 data = sigar.getNfsServerV2();
			cached.put("getNfsServerV2", new SigarData(now + ttl,data));
			return data;
		}
		return (NfsServerV2)found.data();
	}

	@Override
	public NfsServerV3 getNfsServerV3() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getNfsServerV3");
		if (found == null || found.isExpired(now)){
			NfsServerV3 data = sigar.getNfsServerV3();
			cached.put("getNfsServerV3", new SigarData(now + ttl,data));
			return data;
		}
		return (NfsServerV3)found.data();
	}

	@Override
	public long getPid() {
		return sigar.getPid();
	}

	@Override
	public String[] getProcArgs(long arg0) throws SigarException {
		return sigar.getProcArgs(arg0);
	}

	@Override
	public String[] getProcArgs(String arg0) throws SigarException {
		return sigar.getProcArgs(arg0);
	}

	@Override
	public ProcCpu getProcCpu(long arg0) throws SigarException {
		return sigar.getProcCpu(arg0);
	}

	@Override
	public ProcCpu getProcCpu(String arg0) throws SigarException {
		return sigar.getProcCpu(arg0);
	}

	@Override
	public ProcCred getProcCred(long arg0) throws SigarException {
		return sigar.getProcCred(arg0);
	}

	@Override
	public ProcCred getProcCred(String arg0) throws SigarException {
		return sigar.getProcCred(arg0);
	}

	@Override
	public ProcCredName getProcCredName(long arg0) throws SigarException {
		return sigar.getProcCredName(arg0);
	}

	@Override
	public ProcCredName getProcCredName(String arg0) throws SigarException {
		return sigar.getProcCredName(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getProcEnv(long arg0) throws SigarException {
		return sigar.getProcEnv(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getProcEnv(String arg0) throws SigarException {
		return sigar.getProcEnv(arg0);
	}

	@Override
	public String getProcEnv(long arg0, String arg1) throws SigarException {
		return sigar.getProcEnv(arg0, arg1);
	}

	@Override
	public String getProcEnv(String arg0, String arg1) throws SigarException {
		return sigar.getProcEnv(arg0, arg1);
	}

	@Override
	public ProcExe getProcExe(long arg0) throws SigarException {
		return sigar.getProcExe(arg0);
	}

	@Override
	public ProcExe getProcExe(String arg0) throws SigarException {
		return sigar.getProcExe(arg0);
	}

	@Override
	public ProcFd getProcFd(long arg0) throws SigarException {
		return sigar.getProcFd(arg0);
	}

	@Override
	public ProcFd getProcFd(String arg0) throws SigarException {
		return sigar.getProcFd(arg0);
	}

	@Override
	public long[] getProcList() throws SigarException {
		return sigar.getProcList();
	}

	@Override
	public ProcMem getProcMem(long arg0) throws SigarException {
		return sigar.getProcMem(arg0);
	}

	@Override
	public ProcMem getProcMem(String arg0) throws SigarException {
		return sigar.getProcMem(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getProcModules(long arg0) throws SigarException {
		return sigar.getProcModules(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getProcModules(String arg0) throws SigarException {
		return sigar.getProcModules(arg0);
	}

	@Override
	public long getProcPort(int arg0, long arg1) throws SigarException {
		return sigar.getProcPort(arg0, arg1);
	}

	@Override
	public long getProcPort(String arg0, String arg1) throws SigarException {
		return sigar.getProcPort(arg0, arg1);
	}

	@Override
	public ProcStat getProcStat() throws SigarException {
		return sigar.getProcStat();
	}

	@Override
	public ProcState getProcState(long arg0) throws SigarException {
		return sigar.getProcState(arg0);
	}

	@Override
	public ProcState getProcState(String arg0) throws SigarException {
		return sigar.getProcState(arg0);
	}

	@Override
	public ProcTime getProcTime(long arg0) throws SigarException {
		return sigar.getProcTime(arg0);
	}

	@Override
	public ProcTime getProcTime(String arg0) throws SigarException {
		return sigar.getProcTime(arg0);
	}

	@Override
	public ResourceLimit getResourceLimit() throws SigarException {
		//缓存1d
		long ttl = 24 * 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getResourceLimit");
		if (found == null || found.isExpired(now)){
			ResourceLimit data = sigar.getResourceLimit();
			cached.put("getResourceLimit", new SigarData(now + ttl,data));
			return data;
		}
		return (ResourceLimit)found.data();
	}

	@Override
	public long getServicePid(String arg0) throws SigarException {
		return sigar.getServicePid(arg0);
	}

	@Override
	public Swap getSwap() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getSwap");
		if (found == null || found.isExpired(now)){
			Swap data = sigar.getSwap();
			cached.put("getSwap", new SigarData(now + ttl,data));
			return data;
		}
		return (Swap)found.data();
	}

	@Override
	public Tcp getTcp() throws SigarException {
		//缓存10s
		long ttl = 10 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getTcp");
		if (found == null || found.isExpired(now)){
			Tcp data = sigar.getTcp();
			cached.put("getTcp", new SigarData(now + ttl,data));
			return data;
		}
		return (Tcp)found.data();
	}

	@Override
	public Uptime getUptime() throws SigarException {
		//缓存1h
		long ttl = 60 * 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getUptime");
		if (found == null || found.isExpired(now)){
			Uptime data = sigar.getUptime();
			cached.put("getUptime", new SigarData(now + ttl,data));
			return data;
		}
		return (Uptime)found.data();
	}

	@Override
	public Who[] getWhoList() throws SigarException {
		//缓存1m
		long ttl = 60 * 1000;
		long now = System.currentTimeMillis();
		SigarData found = cached.get("getWhoList");
		if (found == null || found.isExpired(now)){
			Who[] data = sigar.getWhoList();
			cached.put("getWhoList", new SigarData(now + ttl,data));
			return data;
		}
		return (Who[])found.data();
	}
	
	/**
	 * 缓存的sigar数据
	 * 
	 * @author duanyy
	 *
	 */
	protected static class SigarData {
		/**
		 * 数据过期时间
		 */
		private long expireTime;
		
		/**
		 * sigar数据
		 */
		private Object data;
		
		public SigarData(long t,Object sigarData){
			expireTime = t;
			data = sigarData;
		}

		public Object data(){
			return data;
		}
		
		/**
		 * 是否过期
		 * @param now 当前时间
		 * @return 是否过期
		 */
		public boolean isExpired(long now){
			return now > expireTime;
		}
	}
	
	/**
	 * 计算速率工具类
	 * @author yyduan
	 *
	 */
	public static class Rate {
		private volatile long value = 0;
		private volatile double lastRate = 0.0f;
		private volatile long timestamp = System.currentTimeMillis();
		
		public synchronized double getRate(long latestValue){
			long now = System.currentTimeMillis();
			long duration = now - timestamp;
			if (duration <= 0){
				return lastRate;
			}
			
			lastRate = (latestValue - value) / duration;			
			value = latestValue;
			timestamp = now;
			return lastRate <= 0 ? 0 : lastRate;
		}
	}
}
