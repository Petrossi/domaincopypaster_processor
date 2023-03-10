/*
 * Copyright 2009 Guy Van den Broeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.domainsurvey.crawler.utils.diff.output;


import com.domainsurvey.crawler.utils.diff.tag.IAtomSplitter;

/**
 * Interface for classes that are interested in the tag-like result structure
 * as produced by DaisyDiff.
 *
 * @author kapelonk
 * @see TagDiffer
 */
public interface TextDiffer {

    /**
     * Compares two Node Trees.
     *
     * @param leftComparator  Root of the first tree.
     * @param rightComparator Root of the second tree.
     * @throws Exception something went wrong with parsing of the trees.
     */
    void diff(IAtomSplitter leftComparator, IAtomSplitter rightComparator)
            throws Exception;
}
