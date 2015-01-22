import os
import os.path
import socket
import subprocess

if  not os.environ.has_key("GEMFIRE"):
	sys.exit("GEMFIRE environment variable must be configured")
	
GEMFIRE=os.environ["GEMFIRE"]



CLUSTER_HOME="/vagrant/tmp/wan-gemfire"
LOCATOR_PID_FILE="cf.gf.locator.pid"
SERVER_PID_FILE="cf.gf.server.pid"

def clusterDir(cnum):
	return  CLUSTER_HOME + "/{0}".format(cnum)
	
def locatorDir(cnum):
	return clusterDir(cnum) + "/locator"
	
def serverDir(cnum, snum):
	return clusterDir(cnum) + "/server_{0}".format(snum)

def ensureDir(dname):
	if not os.path.isdir(dname):
		os.mkdir(dname)
		
	
def ensureDirectories(cnum, nodecount):
	ensureDir(CLUSTER_HOME)
	ensureDir(clusterDir(cnum))
	ensureDir(locatorDir(cnum))
	if nodecount > 0:
		for i in range(1,nodecount + 1):
			ensureDir(serverDir(cnum,i))

def locatorport(cnum):
	return 10000 * cnum			

def serverport(cnum, snum):
	return (10000 * cnum) + (100 * snum)

def pidIsAlive(pidfile):
	if not os.path.exists(pidfile):
		return False
		
	with fopen(pidfile,"r") as f:
		pid = int(f.read())

	psrc = subprocess.call(["ps",str(pid)])
	if psrc == 0:
		return True
	else:
		return False
	

def serverIsRunning(cnum, snum):
	try:
		sock = socket.create_connection(("localhost", serverport(cnum,snum)))
		sock.close()
		return True
	except Exception as x:
		pass
		# ok - probably not running
		
	# now check the pid file
	pidfile = serverDir(cnum,snum) + "/SERVER_PID_FILE"
	return pidIsAlive(pidfile)	
	
def locatorIsRunning(cnum):
	try:
		sock = socket.create_connection(("localhost", locatorport(cnum)))
		sock.close()
		return True
	except Exception as x:
		pass
		# ok - probably not running
		
	# now check the pid file
	pidfile = locatorDir(cnum) + "/LOCATOR_PID_FILE"
	return pidIsAlive(pidfile)	
		
def stopLocator(cnum):
	if not locatorIsRunning(cnum):
		return
		
	subprocess.check_call([GEMFIRE + "/bin/gfsh"
		, "stop", "locator"
		,"--dir=" + locatorDir(cnum)])
		
def startLocator(cnum):
	ensureDirectories(cnum, 0)

	if locatorIsRunning(cnum):
		return
		
	subprocess.check_call([GEMFIRE + "/bin/gfsh"
		, "start", "locator"
		,"--dir=" + locatorDir(cnum)
		,"--port={0}".format(locatorport(cnum))
		,"--name=locator" ])

	
def startCluster(cnum, nodecount):
	ensureDirectories(cnum, nodecount)
	startLocator(cnum)
	processList = []
	dirList = []
	for i in range(1,nodecount + 1):
		if not serverIsRunning(cnum,i):
			proc = subprocess.Popen([GEMFIRE + "/bin/gfsh"
					, "start", "server"
					,"--dir=" + serverDir(cnum, i)
					,"--server-port={0}".format(serverport(cnum,i))
					,"--locators=localhost[{0}]".format(locatorport(cnum))
					,"--name=server_{0}".format(i) ])
			processList.append(proc)
			dirList.append(serverDir(cnum, i))
		
	for j in range(0, len(processList)):
		if processList[j].wait() != 0:
			raise Exception("cache server process failed to start - see the logs in {0}".format(dirList[j]))
			
def stopCluster(cnum):
	if not locatorIsRunning(cnum):
		return
		
	rc = subprocess.call([GEMFIRE + "/bin/gfsh"
		, "-e", "connect --locator=localhost[{0}]".format(locatorport(cnum))
		,"-e", "shutdown"])

	# it appears that the return code in this case is not correct
	# will just hope for the best right now	
	
	stopLocator(cnum)
	

			