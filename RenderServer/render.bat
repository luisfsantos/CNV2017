REM the line below makes sure it starts in the directory where the script resides
cd %~dp0

if not exist src\raytracer\instrumented mkdir src\raytracer\instrumented
javac -cp src;lib src\raytracer\*.java src\raytracer\pigments\*.java src\raytracer\shapes\*.java
java -cp src;lib;src\raytracer MetricMethodCount src\raytracer\RayTracer.class src\raytracer\instrumented
move src\raytracer\instrumented\RayTracer.class src\raytracer\
java -cp src;lib;src\raytracer -XX:-UseSplitVerifier webserver.Server
