# CNV2017
Project Repo -- CNV 2017 (Luis Santos Catarina Cepeda e João Peralta)

## Environment
This assumes the AWS sdk has been extracted to /lib 
### Code
```
export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
export CP_CNV=":/lib/aws-java-sdk-1.11.128/lib/*:/lib/aws-java-sdk-1.11.128/third-party/lib/*"
```


## Metrics Storage
This module houses all DynamoDB code and code related to Requests which is necessary for both the LoadBalancer and the RenderServer (worker) packages.
Seeing as the other two have dependancies related to this one run `ant` here first.

### Code
```
ant
```

## Load Balancer
This module is the Load Balancer and has the main method to run the balancer (and the AutoScaler):
```
ant
java -cp $CP_CNV:LoadBalancer.jar loadbalancer.LoadBalancer
```

## Render Server
This module is for the worker which do the actual rendering and has the main method to run the server which awaits render requests:
```
ant
java -cp $CP_CNV:RenderServer.jar webserver.Server
```
__NB__: The Render workers will register with the load balancer so please do run the loadbalancer first if you don't want issues.
