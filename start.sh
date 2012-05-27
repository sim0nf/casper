#!/bin/bash

vertx deploy com.cosm.casper.Reporter -cp classes -worker
vertx deploy com.cosm.casper.Analyzer -cp classes
vertx deploy com.cosm.casper.Proxy -cp classes
