// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.models.Modifier;

import java.util.Locale;

public class ChatCustomizations extends Customization {
    // Packages
    private static final String IMPLEMENTATION_MODELS = "com.azure.android.communication.chat.implementation.models";

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeModelsPackage(libraryCustomization.getPackage(IMPLEMENTATION_MODELS));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        ClassCustomization responseExceptionCls
            = packageCustomization.getClass("CommunicationErrorResponseException")
            .rename("ChatErrorResponseException");
        responseExceptionCls
            .getMethod("getValue")
            .rename("getValueIntern")
            .setModifier(Modifier.PRIVATE)
            .removeAnnotation("Override");
        responseExceptionCls
            .addMethod(
                "/**\n" +
                    " * Get error \n" +
                    " * @return the error.\n" +
                    " */\n" +
                    "@Override\n" +
                    "public CommunicationError getValue() {\n" +
                    "    return this.getValueIntern().getError();\n" +
                    "}"
            );

    }
}
