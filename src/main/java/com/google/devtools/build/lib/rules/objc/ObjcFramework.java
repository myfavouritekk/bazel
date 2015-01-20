// Copyright 2014 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.rules.objc;

import static com.google.devtools.build.lib.collect.nestedset.Order.STABLE_ORDER;

import com.google.common.base.Optional;
import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.analysis.ConfiguredTarget;
import com.google.devtools.build.lib.analysis.RuleConfiguredTarget.Mode;
import com.google.devtools.build.lib.analysis.RuleContext;
import com.google.devtools.build.lib.collect.nestedset.NestedSetBuilder;
import com.google.devtools.build.lib.rules.RuleConfiguredTargetFactory;
import com.google.devtools.build.lib.rules.objc.ObjcSdkFrameworks.Attributes;

/**
 * Implementation for the {@code objc_framework} rule.
 */
public class ObjcFramework implements RuleConfiguredTargetFactory {
  @Override
  public ConfiguredTarget create(RuleContext ruleContext) throws InterruptedException {
    Attributes sdkFrameworkAttributes = new Attributes(ruleContext);

    ObjcCommon common = new ObjcCommon.Builder(ruleContext)
        .addFrameworkImports(
            ruleContext.getPrerequisiteArtifacts("framework_imports", Mode.TARGET).list())
        .addExtraSdkFrameworks(sdkFrameworkAttributes.sdkFrameworks())
        .addExtraWeakSdkFrameworks(sdkFrameworkAttributes.weakSdkFrameworks())
        .addExtraSdkDylibs(sdkFrameworkAttributes.sdkDylibs())
        .build();
    common.reportErrors();
    return common.configuredTarget(
        NestedSetBuilder.<Artifact>emptySet(STABLE_ORDER) /* filesToBuild */,
        Optional.<XcodeProvider>absent(),
        Optional.of(common.getObjcProvider()),
        Optional.<J2ObjcSrcsProvider>absent());
  }
}