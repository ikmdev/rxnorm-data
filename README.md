# rxnorm-data

### Team Ownership - Product Owner
Data Team

## Getting Started

Follow these instructions to generate a rxnorm dataset:

1. Clone the [rxnorm-data repository](https://github.com/ikmdev/rxnorm-data)

```bash
git clone [Rep URL]
```

2. Change local directory to `rxnorm-data`

3. Download US or International RF2 Files from SNOMED CT: https://www.nlm.nih.gov/healthit/snomedct/index.html

4. Place the downloaded SnomedCT_*_.zip in your local Downloads directory.

5. Ensure the rxnorm-data/pom.xml contains the proper tags containing source filename for the downloaded files such as:
   <source.zip>, <source.version>, <snomed.source.zip>, starterSet, etc.

6. Create a ~/Solor directory and ensure ~/Solor/generated-data does not exist or is empty.

7. Enter the following command to build the dataset:

```bash
mvn clean install -U "-DMaven.build.cache.enable=false"
```

8. Enter the following command to deploy the dataset:

```bash
mvn deploy -f snomed-ct-export "-DdeployToNexus=true" "-Dmaven.deploy.skip=true" "-Dmaven.build.cache.enabled=false"
```

9. You can create a reasoned or unreasoned dataset by either including or commenting out the rxnorm-data/pom.xml <module>rxnorm-reasoner</module>

