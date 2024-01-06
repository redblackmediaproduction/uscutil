# Universal SmartCard Utility (uscutil)

## Purpose
uscutil aims to provide everything needed to interface with common smartcards (i.e. that can be communicated with using the cross-platform PC/SC API).

It provides three libraries as well as a JavaFX-based GUI:

1. libusc: this library contains wrapper code for the Java SmartCard API (that itself wraps PC/SC) as well as common abstractions for various card functionalities (e.g. ICAO 9303-compliant travel documents)
2. libmrz: this library contains a parser and formatter for ICAO 9303-compliant "machine readable zones"
3. libmrz_cv: this library contains glue code to OpenCV to recognize and extract ICAO 9303-compliant MRZs from still images

## Major external dependencies

* OpenCV and [webcam-capture](https://github.com/sarxos/webcam-capture) for webcam image acquisition
* Leptonica and Tesseract for MRZ OCR (packaged by [bytedeco / JavaCV](https://github.com/bytedeco/javacv))

## Features

- [ ] Foo

## Resources

* Machine-readable travel documents: [ICAO 9303](https://www.icao.int/publications/pages/publication.aspx?docnum=9303)
* German Personalausweis: [BSI TR 03127](https://www.bsi.bund.de/SharedDocs/Downloads/DE/BSI/Publikationen/TechnischeRichtlinien/TR03127/BSI-TR-03127.pdf?__blob=publicationFile&v=2)
* 