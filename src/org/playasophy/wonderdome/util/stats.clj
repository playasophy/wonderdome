(ns org.playasophy.wonderdome.util.stats
  "Functions to read the current system and JVM status."
  (import
    com.sun.management.OperatingSystemMXBean
    java.lang.management.ManagementFactory))


(defn system-properties
  "Returns a map of status info from the Java system properties."
  []
  {:java/vm-name (System/getProperty "java.vm.name")   ; OpenJDK 64-bit Server VM
   :java/version (System/getProperty "java.version")   ; 1.7.0_55-b14
   :os/arch      (System/getProperty "os.arch")        ; amd64
   :os/name      (System/getProperty "os.name")        ; Linux
   :os/version   (System/getProperty "os.version")})   ; 3.13.0-30-generic


(defn os-info
  "Returns a map of cpu and memory utilization info from the operating system
  MX bean."
  []
  (when-let [os-bean (ManagementFactory/getOperatingSystemMXBean)]
    {:cpu.load/system (.getSystemLoadAverage os-bean)
     :cpu.load/process (.getProcessCpuLoad os-bean)
     :memory.physical/free (.getFreePhysicalMemorySize os-bean)
     :memory.physical/total (.getTotalPhysicalMemorySize os-bean)
     :memory.swap/free (.getFreeSwapSpaceSize os-bean)
     :memory.swap/total (.getTotalSwapSpaceSize os-bean)
     :memory.virtual/committed (.getCommittedVirtualMemorySize os-bean)}))


(defn thread-info
  "Returns a map of thread count info from the thread MX bean."
  []
  (when-let [thread-bean (ManagementFactory/getThreadMXBean)]
    {:thread/daemons (.getDaemonThreadCount thread-bean)
     :thread/count (.getThreadCount thread-bean)
     :thread/peak (.getPeakThreadCount thread-bean)}))


(defn info
  "Returns a combined map of all stat info."
  []
  (merge (system-properties) (os-info) (thread-info)))
