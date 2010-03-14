package org.kar

/**
 * @author Kelly Robinson
 */
class CaptchaBreakerTest extends GroovyTestCase
{
    public void testPrintImage()
    {
        def breaker = new CaptchaBreaker()
        /* tesseract interprets "e4ya" as "e4ga" unfortunately */ 
        ['9koO', 'jxt9'/*,'e4ya'*/].each {String imageName ->
            def fileName = "src/test/resources/${imageName}.gif"
            assertEquals("Testing $imageName",imageName, breaker.imageToString(fileName))
        }
    }
}
