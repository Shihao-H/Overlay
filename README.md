
## Overlay


### Usage:

Place the ***makefile*** in current directory.

```sh
$ mkdir bin
$ cd ..
$ make
$ cd bin
```

```sh
$ java cs455.overlay.node.Registry portnum
```

The launch another terminal and cd into the bin folder.

```sh
$ cd bin
```

Make sure you place the ***runClient.sh*** and ***machine_list*** file inside the bin folder as well. the script will look for it.  machine_list has 10 machines and specify the argument to 1 can launch 20 machines.

```sh
$ runClient.sh 1
```

* Clean the .class file in bin folder

```sh
$ make clean
```

