/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.muni.fi.mir.mathmlcaneval.mappers;

import cz.muni.fi.mir.mathmlcaneval.domain.User;
import cz.muni.fi.mir.mathmlcaneval.security.MathUser;
import cz.muni.fi.mir.mathmlcaneval.security.MathUserImpl;
import java.util.Collections;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

  default MathUser map(User user) {
    return new MathUserImpl(user.getId(), user.getUsername(), Collections.emptySet(),
      user.getPassword());
  }
}
