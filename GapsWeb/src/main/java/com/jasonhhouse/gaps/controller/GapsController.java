/*
 * Copyright 2020 Jason H House
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.jasonhhouse.gaps.controller;

import com.jasonhhouse.gaps.Payload;
import com.jasonhhouse.gaps.properties.PlexProperties;
import com.jasonhhouse.gaps.service.IoService;
import java.io.IOException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = "/")
public class GapsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GapsController.class);

    private final IoService ioService;

    @Autowired
    public GapsController(IoService ioService) {
        this.ioService = ioService;
    }

    @GetMapping(value = "/home",
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getIndexOnClick() {
        LOGGER.info("getIndexOnClick()");

        PlexProperties plexProperties;
        try {
            plexProperties = ioService.readProperties();
        } catch (IOException e) {
            LOGGER.error("Failed to read plex properties. Probably the first run.", e);
            plexProperties = new PlexProperties();
        }

        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("plexProperties", plexProperties);
        return modelAndView;
    }

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getIndex() {
        LOGGER.info("getIndex()");

        PlexProperties plexProperties;
        try {
            plexProperties = ioService.readProperties();
        } catch (IOException e) {
            LOGGER.error("Failed to read plex properties. Probably the first run.", e);
            plexProperties = new PlexProperties();
        }

        //If configuration is filled in, jump to libraries page
        if (plexProperties != null && StringUtils.isNotEmpty(plexProperties.getMovieDbApiKey()) && CollectionUtils.isNotEmpty(plexProperties.getPlexServers())) {
            return new ModelAndView("redirect:/libraries");
        }


        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("plexProperties", plexProperties);
        return modelAndView;
    }

    @PutMapping(value = "/nuke",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Payload> putNuke() {
        LOGGER.info("putNuke()");
        LOGGER.info("Deleting all local files");
        Payload payload = ioService.nuke();
        return ResponseEntity.ok().body(payload);
    }

    @GetMapping(value = "/about",
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getAbout() {
        LOGGER.info("getAbout()");

        return new ModelAndView("about");
    }

    @GetMapping(value = "/login",
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getLogin() {
        LOGGER.info("getLogin()");

        return new ModelAndView("login");
    }

    @GetMapping(value = "/updates",
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getUpdates() {
        LOGGER.info("getUpdates()");

        return new ModelAndView("updates");
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        LOGGER.info("initBinder()");
    }
}
