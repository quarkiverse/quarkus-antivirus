<div align="center">
<img src="https://github.com/quarkiverse/quarkus-antivirus/blob/main/docs/modules/ROOT/assets/images/quarkus.svg" width="67" height="70" ><img src="https://github.com/quarkiverse/quarkus-antivirus/blob/main/docs/modules/ROOT/assets/images/plus-sign.svg" height="70" ><img src="https://github.com/quarkiverse/quarkus-antivirus/blob/main/docs/modules/ROOT/assets/images/antivirus-protection-icon.svg" height="70" >

# Quarkus Antivirus
</div>
<br>

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.antivirus/quarkus-antivirus?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.antivirus/quarkus-antivirus)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://github.com/quarkiverse/quarkus-antivirus/actions/workflows/build.yml/badge.svg)](https://github.com/quarkiverse/quarkus-antivirus/actions/workflows/build.yml)

<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-3-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

A Quarkus extension that lets you scan files for viruses using a pluggable engine architecture.

Out of the box these engines are supported by this extension:
- [ClamAV](https://www.clamav.net/) which is a Linux Native antivirus server
- [VirusTotal](https://www.virustotal.com/) which is a REST API to check the Hash of a file to see if it has already been reported for viruses

## Getting started

Read the full [Antivirus documentation](https://docs.quarkiverse.io/quarkus-antivirus/dev/index.html).

### Prerequisite

- Create or use an existing Quarkus application
- Add the Antivirus extension

### Installation

Create a new Antivirus project (with a base Antivirus starter code):

- With [code.quarkus.io](https://code.quarkus.io/?a=Antivirus-bowl&j=17&e=io.quarkiverse.antivirus%3Aquarkus-antivirus)
- With the [Quarkus CLI](https://quarkus.io/guides/cli-tooling):

```bash
quarkus create app antivirus-app -x=io.quarkiverse.antivirus:quarkus-antivirus
```
Or add to you pom.xml directly:

```xml
<dependency>
    <groupId>io.quarkiverse.antivirus</groupId>
    <artifactId>quarkus-antivirus</artifactId>
    <version>{project-version}</version>
</dependency>
```

## Configuration

Now that you configured your POM to use the service, now you need to configure which scanner(s) you want to use in `application.properties`:

### ClamAV

[ClamAV](https://www.clamav.net/) is an open source Linux based virus scanning engine.
If you don't set a host `quarkus.antivirus.clamav.host` a DevService will start a ClamAV instance for you on a dynamic free port, so you can test locally during development.

```properties
quarkus.antivirus.clamav.enabled=true
```

#### ClamAV Health Check

If you are using the `quarkus-smallrye-health` extension,
quarkus-vault can add a readiness health check to validate the connection to the ClamAV server.

If enabled (by default) and the extension is present,
when you access the `/q/health/ready` endpoint of your application you will have information about the connection validation status.

You can disable this behavior by setting the property `quarkus.antivirus.clamav.health.enabled` to `false` in your application.properties.

### VirusTotal

[VirusTotal](https://www.virustotal.com/) is a REST API that analyses suspicious files to detect malware using over 70 antivirus scanners.  VirusTotal checks the hash of a file to see if it has been scanned and what the results are.  You can set the threshold of how many of the 70+ engines you want to report the file as malicious before you consider it a malicious file using the `minimum-votes` property.

```properties
quarkus.antivirus.virustotal.enabled=true
quarkus.antivirus.virustotal.key=<YOUR API KEY>
quarkus.antivirus.virustotal.minimum-votes=1
```

## Usage

Simply inject the `Antivirus` service, and it will run the scan against all configured services.  It works against `InputStream` so it can be used in any Quarkus application it is not constrained to REST applications only.

```java
@Path("/antivirus")
@ApplicationScoped
public class AntivirusResource {

    @Inject
    Antivirus antivirus;

    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    public Response upload(@MultipartForm @Valid final UploadRequest fileUploadRequest) {
        final String fileName = fileUploadRequest.getFileName();
        final InputStream data = fileUploadRequest.getData();
        try {
            // copy the stream to make it resettable
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    IOUtils.toBufferedInputStream(data).readAllBytes());

            // scan the file and check the results
            List<AntivirusScanResult> results = antivirus.scan(fileName, inputStream);
            for (AntivirusScanResult result : results) {
                if (result.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new WebApplicationException(result.getMessage(), result.getStatus());
                }
            }

            // write the file out to disk
            final File tempFile = File.createTempFile("fileName", "tmp");
            IOUtils.copy(inputStream, new FileOutputStream(tempFile));
        } catch (IOException e) {
            throw new BadRequestException(e);
        }

        return Response.ok().build();
    }
}
```

## Pluggable
We can't anticipate every antivirus engine out there and some may be proprietary.  However, the architecture is designed to be pluggable, so you can plug your own engine in.  Simply produce a bean that extends the `AntivirusEngine` interface and it will be picked up and used.

```java
@ApplicationScoped
public class MyCustomEngine implements AntivirusEngine {
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public AntivirusScanResult scan(final String filename, final InputStream inputStream) {
        // scan your file here
    }
}
```

## 🧑‍💻 Contributing

- Contribution is the best way to support and get involved in community!
- Please, consult our [Code of Conduct](./CODE_OF_CONDUCT.md) policies for interacting in our community.
- Contributions to `quarkus-antivirus` Please check our [CONTRIBUTING.md](./CONTRIBUTING.md)

### If you have any idea or question 🤷

- [Ask a question](https://github.com/quarkiverse/quarkus-antivirus/discussions)
- [Raise an issue](https://github.com/quarkiverse/quarkus-antivirus/issues)
- [Feature request](https://github.com/quarkiverse/quarkus-antivirus/issues)
- [Code submission](https://github.com/quarkiverse/quarkus-antivirus/pulls)
## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="http://melloware.com"><img src="https://avatars.githubusercontent.com/u/4399574?v=4?s=100" width="100px;" alt="Melloware"/><br /><sub><b>Melloware</b></sub></a><br /><a href="#maintenance-melloware" title="Maintenance">🚧</a> <a href="https://github.com/quarkiverse/quarkus-antivirus/commits?author=melloware" title="Code">💻</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/ketola"><img src="https://avatars.githubusercontent.com/u/966606?v=4?s=100" width="100px;" alt="Sauli Ketola"/><br /><sub><b>Sauli Ketola</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-antivirus/commits?author=ketola" title="Tests">⚠️</a> <a href="#ideas-ketola" title="Ideas, Planning, & Feedback">🤔</a></td>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/ggrebert"><img src="https://avatars.githubusercontent.com/u/1737774?v=4?s=100" width="100px;" alt="Geoffrey GREBERT"/><br /><sub><b>Geoffrey GREBERT</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-antivirus/commits?author=ggrebert" title="Code">💻</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
