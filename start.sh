#!/bin/bash

vertx deploy com.cosm.casper.Analyzer -cp classes
#vertx deploy com.cosm.casper.Proxy -cp classes
vertx deploy com.cosm.casper.FakeProxy -cp classes
vertx deploy com.cosm.casper.SockGuy -cp classes

