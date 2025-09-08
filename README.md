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

3. Download RxNorm File from National Library of Medicine (RxNorm-in-OWL/RxNorm Files): https://www.nlm.nih.gov/research/umls/rxnorm/index.html

4. Place the downloaded Pilot-Defined-RxNorm_*_.owl in your local Downloads directory.

5. Ensure the rxnorm-data/pom.xml contains the proper tags containing source filename for the downloaded files such as:
   <source.zip>, <source.version>, <snomed.source.zip>, <starterSet>, etc.

6. Create a ~/Solor directory and ensure ~/Solor/generated-data does not exist or is empty.

7. You can create a reasoned or unreasoned dataset by either including or commenting out the rxnorm-data/pom.xml <module>rxnorm-reasoner</module>

8. Enter the following command to build the dataset:

```bash
mvn clean install -U "-DMaven.build.cache.enable=false"
```

9. Enter the following command to deploy the dataset:

```bash
mvn deploy -f rxnorm-export "-DdeployToNexus=true" "-Dmaven.deploy.skip=true" "-Dmaven.build.cache.enabled=false"
```

- NOTE. This repo is built on top of an unreasoned spined array DB from snomed-ct-data. Therefore, make sure you have it built before running step #8.

