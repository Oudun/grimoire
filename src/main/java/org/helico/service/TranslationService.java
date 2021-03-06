package org.helico.service;

import org.helico.domain.Translator;
import org.helico.domain.TranslatorProvider;

import java.util.List;

public interface  TranslationService {

    public Translator getTranslator(Long transId);

    public TranslatorProvider getProvider(Long providerId);

    public void translateText(Long dictId);

    public void translateText(Long dictId, Long translatorId);

    public boolean isTranslated(Long wordId, Long translationServiceId);

    public void storeTranslation(Long wordId, Long translatorId, String values);

    public List<TranslatorProvider> listProviders();

    public List<TranslatorProvider> listProviders(String langId);

    public List<Translator> listTranslators(String langId);

}
