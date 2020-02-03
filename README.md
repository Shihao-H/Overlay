
## Scalable-server-design


### Usage:

Place the ***makefile*** in current directory.

```sh
$ mkdir bin
$ cd ..
$ make
$ cd bin
```

```sh
$ java cs455.scaling.server.Server portnum thread-pool-size batch-size batch-time
```

The launch another terminal and cd into the bin folder.

```sh
$ cd bin
```

Make sure you place the ***runClient.sh*** and ***machine_list*** file inside the bin folder as well. the script will look for it.  machine_list has 20 machines and specify the argument to 5 can launch 100 clients.

```sh
$ runClient.sh 5
```

* Clean the .class file in bin folder

```sh
$ make clean
```

