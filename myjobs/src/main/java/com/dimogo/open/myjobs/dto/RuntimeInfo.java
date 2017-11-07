package com.dimogo.open.myjobs.dto;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by Ethan Xiao on 2017/4/27.
 */
public class RuntimeInfo {
	private static final Logger logger = Logger.getLogger(RuntimeInfo.class);
	private Date time;
	private String javaLibraryPath = System.getProperty("java.library.path");

	//vm
	private String ip;
	private String hostName;
	private long vmTotalMemory;
	private long vmFreeMemory;
	private int javaAvailableProcessors;

	//memory and swap
	private long totalMemory;
	private long usedMemory;
	private long freeMemory;
	private long totalSwap;
	private long usedSwap;
	private long freeSwap;

	//OS
	private String osArch;
	private String osCpuEndian;
	private String osDataModel;
	private String osDescription;
	private String osVendor;
	private String osVendorCodeName;
	private String osVendorName;
	private String osVendorVersion;
	private String osVersion;

	private List<CpuRuntimeInfo> cpus;
	private List<DiskRuntimeInfo> disks;
	private List<NetRuntimeInfo> nets;
	private List<EthernetRuntimeInfo> ethernets;
	private Map<String, String> systemProperties;
	private Map<String, String> systemEnv;

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public void setJavaLibraryPath(String javaLibraryPath) {
		this.javaLibraryPath = javaLibraryPath;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setVmTotalMemory(long vmTotalMemory) {
		this.vmTotalMemory = vmTotalMemory;
	}

	public void setVmFreeMemory(long vmFreeMemory) {
		this.vmFreeMemory = vmFreeMemory;
	}

	public void setJavaAvailableProcessors(int javaAvailableProcessors) {
		this.javaAvailableProcessors = javaAvailableProcessors;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}

	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	public void setTotalSwap(long totalSwap) {
		this.totalSwap = totalSwap;
	}

	public void setUsedSwap(long usedSwap) {
		this.usedSwap = usedSwap;
	}

	public void setFreeSwap(long freeSwap) {
		this.freeSwap = freeSwap;
	}

	public void setOsArch(String osArch) {
		this.osArch = osArch;
	}

	public void setOsCpuEndian(String osCpuEndian) {
		this.osCpuEndian = osCpuEndian;
	}

	public void setOsDataModel(String osDataModel) {
		this.osDataModel = osDataModel;
	}

	public void setOsDescription(String osDescription) {
		this.osDescription = osDescription;
	}

	public void setOsVendor(String osVendor) {
		this.osVendor = osVendor;
	}

	public void setOsVendorCodeName(String osVendorCodeName) {
		this.osVendorCodeName = osVendorCodeName;
	}

	public void setOsVendorName(String osVendorName) {
		this.osVendorName = osVendorName;
	}

	public void setOsVendorVersion(String osVendorVersion) {
		this.osVendorVersion = osVendorVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public void setCpus(List<CpuRuntimeInfo> cpus) {
		this.cpus = cpus;
	}

	public void setDisks(List<DiskRuntimeInfo> disks) {
		this.disks = disks;
	}

	public void setNets(List<NetRuntimeInfo> nets) {
		this.nets = nets;
	}

	public void setEthernets(List<EthernetRuntimeInfo> ethernets) {
		this.ethernets = ethernets;
	}

	public void setSystemProperties(Map<String, String> systemProperties) {
		this.systemProperties = systemProperties;
	}

	public String getJavaLibraryPath() {
		return javaLibraryPath;
	}

	public String getIp() {
		return ip;
	}

	public String getHostName() {
		return hostName;
	}

	public long getVmTotalMemory() {
		return vmTotalMemory;
	}

	public long getVmFreeMemory() {
		return vmFreeMemory;
	}

	public int getJavaAvailableProcessors() {
		return javaAvailableProcessors;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public long getUsedMemory() {
		return usedMemory;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public long getTotalSwap() {
		return totalSwap;
	}

	public long getUsedSwap() {
		return usedSwap;
	}

	public long getFreeSwap() {
		return freeSwap;
	}

	public String getOsArch() {
		return osArch;
	}

	public String getOsCpuEndian() {
		return osCpuEndian;
	}

	public String getOsDataModel() {
		return osDataModel;
	}

	public String getOsDescription() {
		return osDescription;
	}

	public String getOsVendor() {
		return osVendor;
	}

	public String getOsVendorCodeName() {
		return osVendorCodeName;
	}

	public String getOsVendorName() {
		return osVendorName;
	}

	public String getOsVendorVersion() {
		return osVendorVersion;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public List<CpuRuntimeInfo> getCpus() {
		return cpus;
	}

	public List<DiskRuntimeInfo> getDisks() {
		return disks;
	}

	public List<NetRuntimeInfo> getNets() {
		return nets;
	}

	public List<EthernetRuntimeInfo> getEthernets() {
		return ethernets;
	}

	public Map<String, String> getSystemProperties() {
		return systemProperties;
	}

	public Map<String, String> getSystemEnv() {
		return systemEnv;
	}

	public void setSystemEnv(Map<String, String> systemEnv) {
		this.systemEnv = systemEnv;
	}

	public double getCpusUsedPercent() {
		if (CollectionUtils.isEmpty(cpus)) {
			return 0d;
		}
		double usedPercent = 0;
		for (CpuRuntimeInfo cpu : cpus) {
			usedPercent += cpu.getCombined();
		}
		return usedPercent;
	}

	public double getDisksUsedPercent() {
		if (CollectionUtils.isEmpty(disks)) {
			return 0d;
		}
		double usedPercent = 0;
		for (DiskRuntimeInfo disk : disks) {
			usedPercent += disk.getUsedPercent();
		}
		return usedPercent * 100;
	}

	public RuntimeInfo() {

	}

	public void setup() {
		time = new Date();
		InetAddress addr;
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}

		Runtime r = Runtime.getRuntime();
		vmTotalMemory = r.totalMemory();
		vmFreeMemory = r.freeMemory();
		javaAvailableProcessors = r.availableProcessors();

		systemProperties = new LinkedHashMap<String, String>((Map)System.getProperties());
		systemEnv = System.getenv();

		Sigar sigar;
		try {
			sigar = new Sigar();
		} catch (Throwable e) {
			return;
		}

		try {
			Mem mem = sigar.getMem();
			totalMemory = mem.getTotal();
			usedMemory = mem.getUsed();
			freeMemory = mem.getFree();
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}

		try {
			Swap swap = sigar.getSwap();
			totalSwap = swap.getTotal();
			usedSwap = swap.getUsed();
			freeSwap = swap.getFree();
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}

		try {
			CpuInfo infos[] = sigar.getCpuInfoList();
			CpuPerc cpuList[] = sigar.getCpuPercList();
			cpus = new LinkedList<CpuRuntimeInfo>();
			for (int i = 0; i < infos.length; i++) {
				cpus.add(new CpuRuntimeInfo(infos[i], cpuList[i]));
			}
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}

		try {
			OperatingSystem OS = OperatingSystem.getInstance();
			osArch = OS.getArch();
			osCpuEndian = OS.getCpuEndian();
			osDataModel = OS.getDataModel();
			osDescription = OS.getDescription();
			osVendor = OS.getVendor();
			osVendorCodeName = OS.getVendorCodeName();
			osVendorName = OS.getVendorName();
			osVendorVersion = OS.getVendorVersion();
			osVersion = OS.getVersion();
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}

		try {
			FileSystem fslist[] = sigar.getFileSystemList();
			disks = new LinkedList<DiskRuntimeInfo>();
			for (FileSystem fs : fslist) {
				disks.add(new DiskRuntimeInfo(fs, sigar.getFileSystemUsage(fs.getDirName())));
			}
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}

		try {
			String ifNames[] = sigar.getNetInterfaceList();
			nets = new LinkedList<NetRuntimeInfo>();
			ethernets = new LinkedList<EthernetRuntimeInfo>();
			for (String name : ifNames) {
				NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
				NetInterfaceStat ifstat = ((ifconfig.getFlags() & 1L) <= 0L) ? null : sigar.getNetInterfaceStat(name);
				nets.add(new NetRuntimeInfo(ifconfig, ifstat));
				if (NetFlags.LOOPBACK_ADDRESS.equals(ifconfig.getAddress()) || (ifconfig.getFlags() & NetFlags.IFF_LOOPBACK) != 0
						|| NetFlags.NULL_HWADDR.equals(ifconfig.getHwaddr())) {
					continue;
				}
				ethernets.add(new EthernetRuntimeInfo(ifconfig));
			}
		} catch (Throwable e) {
			if (logger.isDebugEnabled()) {
				logger.debug(e);
			}
		}
	}
}
