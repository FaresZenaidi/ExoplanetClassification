# ExoplanetClassification
This repository was created to gather a practical work on exoplanets classification, developed for the Spark's course of the Big Data program at Telecom ParisTech. 

## Context
**Objective**: Build a Binary Classifier of exoplanets labeled "confirmed" or "false-positive".
**Context**: Exoplanets are planets rotating around other stars than the Sun. Their study allows us to better understand how the solar system was formed, and a fraction of them could be conducive to the development of extraterrestrial life. 

They are detected in two steps:
* A *Satellite* (Kepler) observes the stars and marks those whose luminosity curve shows a "hollow", which could indicate that a planet has passed (part of the light emitted by the star being obscured by the passage of the planet). This method of "transit" allows us to define candidate exoplanets, and to deduce the characteristics that the planet would have if it really existed (distance to its star, diameter, shape of its orbit, etc.).
* It is then necessary to validate or invalidate the candidates using another more expensive method, based on measurements of radial velocities of the star. Candidates are then classified as "confirmed" or "false-positive".

As there are about 200 billion stars in our galaxy, and therefore potentially as much (or even more) exoplanets, their detection must be automated to "scale up". The method of transits is already automatic (more than 22 million curves of luminosity recorded by Kepler), but not the confirmation of the candidate planets, hence the automatic classifier that we will build.
