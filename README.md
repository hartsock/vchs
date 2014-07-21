# VMWare Samples: OnDemand REST API Java Samples


After the initial clone operation (git clone https://github.com/vmware/vchs.git) to your local
system, two provided libraries are required to be installed for compilation to work. Execute the
following commands:

<pre>
mvn install:install-file -Dfile=./lib/vchs-rest-apis-1.0.0.jar -DgroupId=com.vmware.vchs -DartifactId=vchs-rest-apis -Dversion=1.0.0 -Dpackaging=jar
mvn install:install-file -Dfile=./lib/rest-api-schemas-1.0.0.jar -DgroupId=com.vmware.vcloud -DartifactId=rest-api-schemas -Dversion=1.0.0 -Dpackaging=jar
</pre>

After this, compilation will work:

<pre>
mvn compile
</pre>

Once compilation is completed, you can execute the various OnDemand samples against our production
server with your account credentials.
