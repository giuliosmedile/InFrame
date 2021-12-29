package com.smeds.inframe.data

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * Static class that allows to adapt a image or video to fit the screen, in accordance to parameters sent by server.
 * Methods require three pairs of values:
 *  - size of image (or video) in inches
 *  - translation in 100ths of inches (centi-inches?)
 *  - rotation in degrees (TODO)
 *
 *  No class is required since those are kinda-static methods
 */

object MatrixTransform {
    /**
     * Requires a Canvas to work
     */
    fun calcMatrixForImage(
        d : DeviceInfo,
        image: Bitmap,
        translateX: Float,
        translateY: Float,
        sizeOfImageInInchesX: Float,
        sizeOfImageInInchesY: Float
    ) : Matrix {

        val matrix = Matrix()
        // Rendi l'immagine tanto larga quanto il telefono
        val originalWidth: Float = image.getWidth().toFloat()
        val originalHeight: Float = image.getHeight().toFloat()
        val phoneScaleX: Float = d.screenWidthDp / originalWidth
        val phoneScaleY = d.screenHeightDp / originalHeight

        // Scala l'immagine per arrivare alle dimensioni fisiche desiderate
        val finalScaleX = (sizeOfImageInInchesX / d.screenWidthInch).toFloat()
        val finalScaleY = (sizeOfImageInInchesY / d.screenHeightInch).toFloat()

        // Trasla l'immagine alle coordinate desiderate
        // ho bisogno del * 100 perchè l'immagine originale è 1000 x 1000
        val finalTranslateX = (originalWidth / (sizeOfImageInInchesX * 100)) * translateX
        val finalTranslateY = (originalHeight / (sizeOfImageInInchesY * 100)) * translateY

        matrix.preScale(phoneScaleX, phoneScaleY)
        matrix.postScale(finalScaleX, finalScaleY)
        matrix.preTranslate(finalTranslateX, finalTranslateY)

        return matrix
    }

    /**
     * Requires a textureView to work
     */
    fun calcMatrixForVideo (
        d : DeviceInfo,
        translateX: Float,
        translateY: Float,
        sizeOfImageInInchesX: Float,
        sizeOfImageInInchesY: Float
    ) : Matrix {

        val matrix = Matrix()

        // Scala l'immagine alle dimensioni desiderate
        val scaleX : Float = (sizeOfImageInInchesX / d.screenWidthInch).toFloat()
        val scaleY : Float = (sizeOfImageInInchesY / d.screenHeightInch).toFloat()

        // Trasla l'immagine alle dimensioni desiderate
        // Il /100 serve perchè i valori di traslazione vengono riportati in centesimi di pollice
        // La seconda divisione serve perchè la traslazione in textureView avviene per pixel, non per dpi
        // Quindi essenzialmente scala 1px in centesimi di pollice
        // "Trust me, it just works"
        val finalTranslateX : Float = translateX/100 * 1/(d.screenWidthInch / d.screenWidthPx).toFloat()
        val finalTranslateY : Float = translateY/100 * 1/(d.screenHeightInch / d.screenHeightPx).toFloat()

        matrix.preScale(scaleX, scaleY)
        matrix.postTranslate(finalTranslateX, finalTranslateY)

        return matrix

    }

}