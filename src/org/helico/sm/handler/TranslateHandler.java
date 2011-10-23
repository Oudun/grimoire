package org.helico.sm.handler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.helico.domain.Dict;
import org.helico.domain.DictWord;
import org.helico.domain.Job;
import org.helico.domain.Word;
import org.helico.service.DictWordService;
import org.helico.service.JobService;
import org.helico.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;


@Component("translateHandler")
public class TranslateHandler extends AbstractHandler {

    private static final Logger LOG = Logger.getLogger(TranslateHandler.class);

    private static final MessageFormat BING_FORMAT = new MessageFormat ("<{0}>{1}</{2}>");

    private static final int WORDS_PORTION = 32;

    @Autowired
    DictWordService dictWordService;

    @Autowired
    JobService jobService;

    @Autowired
    TranslationService transService;

    HttpClient httpClient;

    public void process(Object data, Job job) throws Exception {
        Dict dict = dictService.findDict(job.getDictId());
        LOG.debug("dict=" + dict);
        Long wordsNum = dictWordService.countWords(dict.getId());
        int offset = 0;
        while (wordsNum > offset) {
            List<DictWord> dictWords = dictWordService.getWords(dict.getId(), offset, WORDS_PORTION);
            for (DictWord dictWord : dictWords) {
                Word word = dictWord.getWord();
                if (!transService.isTranslated(word.getId(), 0L)) {
                    //String translation = word.getValue() + "_translated"; //todo get real translation here
                    String translation = fetchTranslation(word.getValue(), dict.getLangId(), "ru");
                    //CDCB8BFFDD9E4C3054316BC629E82D1E39CA585C bing app id
                    if (translation != null) {
                        transService.storeTranslation(word.getId(), 0L, translation);
                    }
                }
            }
            offset += WORDS_PORTION;
            jobService.setProgress(job.getId(), (int)((offset * 100)/wordsNum));
        }

    }

    private String fetchTranslation(String text, String srcLangId, String destLangId) {
        String result = null;
        try {
            if (httpClient == null) {
                httpClient = new HttpClient();
            }
            GetMethod getMethod = new GetMethod("http://api.microsofttranslator.com/V2/Http.svc/Translate");
            NameValuePair[] params = {
                    new NameValuePair("appId", "CDCB8BFFDD9E4C3054316BC629E82D1E39CA585C"),
                    new NameValuePair("text", text),
                    new NameValuePair("from", srcLangId),
                    new NameValuePair("to", destLangId),
            };
            getMethod.setQueryString(params);
            httpClient.executeMethod(getMethod);
            String output = getMethod.getResponseBodyAsString();
            result = (String)BING_FORMAT.parse(output)[1];
        } catch (Exception e) {
            LOG.error("Can not get translation", e);
        }
        return result;
    }

}
