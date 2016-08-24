Author: Shubin Jacob


Project1

Instructions

------------


1/ To launch the Project,

	sh launcher.sh <path to config file> <netid>


2/ To kill all the processes in the dc* machines created by the Project,

	sh cleanup.sh <path to config file> <netid>

	


NOTE: 
launcher.sh will :

- parse through config file to get the machine nodes.

- compile Project1.java

- launch the nodes mentioned.

Hence, no manual intervention is required to compile or initiate the nodes in the dc* machines given in config.txt 
