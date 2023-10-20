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
├── README.md
├── app.config
├── bin
│   ├── app.config
│   ├── files
│   │   ├── 10KB_000000_peer1.txt
│   │   ├── 10KB_000001_peer1.txt
│   │   ├── 10KB_000002_peer1.txt
│   │   ├── 10KB_000003_peer1.txt
│   │   ├── 10KB_000004_peer1.txt
│   │   ├── 10KB_000005_peer1.txt
│   │   ├── 10KB_000006_peer1.txt
│   │   ├── 10KB_000007_peer1.txt
│   │   ├── 10KB_000008_peer1.txt
│   │   └── 10KB_000009_peer1.txt
│   └── src
│       ├── benchmark
│       │   ├── TestDownload.class
│       │   └── TestSearch.class
│       └── peer
│           ├── PeerMain.class
│           ├── client
│           │   └── PeerClient.class
│           ├── entity
│           │   ├── AddressEntity.class
│           │   ├── FileHolderEntity.class
│           │   ├── PeerEntity.class
│           │   └── SearchMessageEntity.class
│           └── server
│               ├── PeerClientHandler.class
│               └── PeerServer.class
├── build.xml
├── files-generator.py
├── output
│   └── CIS
│       ├── Experiment-1
│       │   └── output-CIS-T1.log
│       └── Experiment-2
│           ├── output-CIS-T2-P1.log
│           ├── output-CIS-T2-P2.log
│           ├── output-CIS-T2-P3.log
│           ├── output-CIS-T2-P4.log
│           ├── output-CIS-T2-P5.log
│           ├── output-CIS-T2-P6.log
│           ├── output-CIS-T2-P7.log
│           ├── output-CIS-T2-P8.log
│           └── output-CIS-T2-P9.log
└── src
    ├── benchmark
    │   ├── TestDownload.java
    │   └── TestSearch.java
    └── peer
        ├── PeerMain.java
        ├── client
        │   └── PeerClient.java
        ├── entity
        │   ├── AddressEntity.java
        │   ├── FileHolderEntity.java
        │   ├── PeerEntity.java
        │   └── SearchMessageEntity.java
        └── server
            ├── PeerClientHandler.java
            └── PeerServer.java
```

### Deployment steps

1. Copy the source directory to where you want to deploy the application

2. Go to source directory and run ant command to generate all java bytecode for peer and testing in bin folder.
    ```bash
    ant
    ``` 

3. Generate files with the following script.
    ```bash
    python files-generator.py <peerID>
    ``` 
    The files will be generated in ```bin/files/```.

4. Go to bin folder:
    ```bash
    cd bin
    ``` 
5. Make configurations in ```app.config```:
    ```bash
    id=peer1                    # id of the current peer
    ip=localhost                # ip address of the current peer
    port=6001                   # port of the current peer
    peers=peer2:localhost:6002,peer4:localhost:6004  # peers
    ttl=3                       # ttl
    repl=peer2,peer3            # replication locations (must be neighbor)
    ``` 
6. To start central indexing server app.
    ```bash
    java src.peer.PeerMain
    ``` 
7. (Optional) To run the test.
    ```bash
    java src.benchmark.TestSearch
    # or
    java src.benchmark.TestDownload
    ``` 