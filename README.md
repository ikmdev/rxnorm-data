# RxNorm Pipeline

**Prerequisites**

* JDK 24+
* Maven 3.9.9+
* Nexus Repository (optional)
* SnomedCt Pipeline Artifact (see [snomed-ct-data repository](https://github.com/ikmdev/snomed-ct-data))
 
**Clone Project and Configure Maven Settings**

1. Clone the [rxnorm-data repository](https://github.com/ikmdev/rxnorm-data)

   ```
   git clone https://github.com/ikmdev/rxnorm-data.git
   ```

2. Configure Maven settings.xml based on the [provided sample](https://ikmdev.atlassian.net/wiki/spaces/IKDT/pages/1036648449/Centralized+Documentation+for+Maven+Settings+File+Configuration).

3. Change local directory to `rxnorm-data`

**Run Origin Packaging**

The following source data is required for this pipeline:

* Pilot-Defined-RxNorm-with-SNCT-classes-2024-04-10-with-custom-annotations.owl

More information can be found on National Library of Medicine (RxNorm-in-OWL/RxNorm Files): https://www.nlm.nih.gov/research/umls/rxnorm/index.html

1. Place the downloaded file in your ~/Downloads directory.

2. Ensure the properties defined in rxnorm-data/pom.xml are set to the correct file names:
   - <rxnormOwl>
   - <source.zip>

3. Run origin packaging and deployment.

   To deploy origin artifact to a shared Nexus repository, run the following command, specifying the repository ID and URL in `-DaltDeploymentRepository`
   ```
   mvn clean deploy -f rxnorm-origin -Ptinkarbuild -DaltDeploymentRepository=tinkar-snapshot::https://nexus.tinkar.org/repository/maven-snapshots/ -Dmaven.build.cache.enabled=false
   ```

   To install origin artifact to a local M2 repository, run the following command:
   ```
   mvn clean install -f rxnorm-origin -Ptinkarbuild,generateDataLocal -Dmaven.build.cache.enabled=false
   ```

**Run Transformation Pipeline**

The transformation pipeline can be built after origin data is available in Nexus or a local M2 repository.

1. Ensure the rxnorm-data/pom.xml contains the proper tags containing source filename for the downloaded files such as:
   <source.zip>, <source.version>, <snomedct.version>, <starterSet>, etc.

2. Build the pipeline with the following command:
   ```
   mvn clean install -U -Ptinkarbuild -Dmaven.build.cache.enabled=false
   ```

3. Deploy transformed data artifacts to Nexus, run the following command:
   ```
   mvn deploy -f snomed-ct-export -Ptinkarbuild -DaltDeploymentRepository=tinkar-snapshot::https://nexus.tinkar.org/repository/maven-snapshots/ -Dmaven.build.cache.enabled=false
   ```
   