[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/ancXpPFO)
### CS550 Advanced Operating Systems Programming Assignment 2 Repo
**Illinois Institute of Technology**  

**Team Name**: NBV  
**Students**: 
* Batkhishig Dulamsurankhor (bdulamsurankhor@hawk.iit.edu)   
* Nitin Singh (nsingh33@hawk.iit.edu)  
* Vaishnavi Papudesi Babu (vpapudesibabu@hawk.iit.edu)   

### Used technology

We used java version 17 to write, compile and run the program, and used ant to compile the source code.

### Folder structure

```bash
├── src                         # source code dir
│   ├── benchmark               # test package
│   │   ├── TestDownload.java
│   │   └── TestSearch.java
│   ├── cis                     # cis package
│   │   ├── CisMain.java
│   │   ├── ClientHandler.java
│   │   └── PeerClientEntity.java
│   └── peer                    # peer package
│       ├── PeerClient.java
│       ├── PeerMain.java
│       └── PeerServer.java
├── bin                        # executable binary dir
│   ├── app.config
│   ├── src
│   │   ├── cis
│   │   │   ├── CisMain.class
│   │   │   ├── ClientHandler.class
│   │   │   └── PeerClientEntity.class
│   │   └── peer
│   │       ├── PeerClient.class
│   │       ├── PeerMain.class
│   │       └── PeerServer.class
│   └── zl.txt
├── app.config                  # app config
├── 1_node_10k_KB_strong_scale.log
├── 1_node_1K_MB_strong_scale.log
├── PA1.pdf                     # doc
├── README.md
├── build.xml                   # ant build instr
├── data-plot.py                # test result plotter
├── files-generator.py          # data generator
├── weakscale.log
└── weakscale_node1.log
```

### Running the application

Go to source directory and run ant command to generate all java bytecode for peer, cis and testing in bin folder.
```bash
ant
``` 

Copy the contents in bin folder to desired location.
```bash
scp -r bin/ username@host_address:/path/to/copy
```
Login to the VM and go to the copied location.

Create folder named files. This is where peers store their shared files.
```bash
mkdir files
``` 

You can generate files with the following script.
```bash
python files-generator.py <peerID>
``` 

To start peer application:
```bash
java src.peer.PeerMain
``` 
To start central indexing server app.
```bash
java src.cis.CisMain
``` 
To run the test.
```bash
java src.benchmark.TestSearch
# or
java src.benchmark.TestDownload
``` 


<img width="1136" alt="Screenshot 2023-09-26 at 10 51 43 PM" src="https://github.com/datasys-classrooms/cs550-fall2023-pa1-nbv/assets/145067050/9162f4b6-e2b3-4420-baf6-9a7993cac243">


**Registration:**
<img width="1138" alt="Screenshot 2023-09-26 at 4 44 31 PM" src="https://github.com/datasys-classrooms/cs550-fall2023-pa1-nbv/assets/145067050/daace1d4-3ce4-4fa7-9d3f-87c8254d45e0">

**Weak Scaling Scalability Study**
![image](https://github.com/datasys-classrooms/cs550-fall2023-pa1-nbv/assets/145067050/0a4412e0-9b5a-48b1-b1a6-8db3bd58539d)

**Strong Scaling Scalability Study**
<img width="970" alt="Screenshot 2023-09-28 at 10 50 26 PM" src="https://github.com/datasys-classrooms/cs550-fall2023-pa1-nbv/assets/145067050/29f87772-37f8-4152-8921-3dc1903de605">
